package toolboxtweaks.toolbox;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ToolboxHelper {

    public static final int SCAN_RANGE = 99;

    public static List<ToolboxBlockEntity> getNearestOutsideRange(LevelAccessor world, Player player, int maxAmount) {
        double minRange = ToolboxHandler.getMaxRange(player);
        if (SCAN_RANGE <= minRange) {
            return List.of();
        }
        Vec3 location = player.position();
        Stream<BlockPos> positions = ToolboxHandler.toolboxes.get(world).keySet().stream()
                .filter(p -> ToolboxHandler.distance(location, p) < SCAN_RANGE * SCAN_RANGE)
                .filter(p -> ToolboxHandler.distance(location, p) >= minRange * minRange)
                .sorted(Comparator.comparingDouble(p -> ToolboxHandler.distance(location, p)))
                .limit(maxAmount);
        WeakHashMap<BlockPos, ToolboxBlockEntity> toolboxMap = ToolboxHandler.toolboxes.get(world);
        Objects.requireNonNull(toolboxMap);
        return positions.map(toolboxMap::get).filter(ToolboxBlockEntity::isFullyInitialized).collect(Collectors.toList());
    }

    private static boolean isLinkedWithToolbox(Player player, int slot) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains("CreateToolboxData")) {
            return false;
        }
        CompoundTag toolboxData = player.getPersistentData().getCompound("CreateToolboxData");
        return toolboxData.contains(String.valueOf(slot));
    }

    @Nullable
    public static ToolboxBlockEntity getBoxForSelectedItem(Player player) {
        Level level = player.level();

        List<ToolboxBlockEntity> toolboxes = ToolboxHandler.getNearest(player.level(), player, 8);
        toolboxes.sort(Comparator.comparing(ToolboxBlockEntity::getUniqueId));

        CompoundTag toolboxData = player.getPersistentData().getCompound("CreateToolboxData");

        String slotKey = String.valueOf(player.getInventory().selected);
        if (toolboxData.contains(slotKey)) {
            BlockPos pos = NbtUtils.readBlockPos(toolboxData.getCompound(slotKey).getCompound("Pos"));
            boolean canReachToolbox = ToolboxHandler.distance(player.position(), pos) < SCAN_RANGE * SCAN_RANGE;
            if (canReachToolbox && level.isLoaded(pos)) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof ToolboxBlockEntity toolbox) {
                    return toolbox;
                }
            }
        }
        return null;
    }

    public static int getSuitableHotbarSlot(Inventory inventory) {
        // use an empty slot if available
        for (int i = 0; i < 9; ++i) {
            int slot = (inventory.selected + i) % 9;
            if (inventory.items.get(slot).isEmpty()) {
                return slot;
            }
        }

        /* otherwise, use a slot that is already linked with a toolbox
         * Ignore items marked by IForgeItemStack::isNotReplaceableByPickAction, even in linked slots
         */
        for (int i = 0; i < 9; ++i) {
            int slot = (inventory.selected + i) % 9;
            if (!inventory.items.get(slot).isNotReplaceableByPickAction(inventory.player, slot)
                    && isLinkedWithToolbox(inventory.player, slot)
            ) {
                return slot;
            }
        }

        /* call original in case of mixins by other mods
         * - Tries to find an empty slot first (already handled here)
         * - Then tries to replace an item not marked by IForgeItemStack::isNotReplaceableByPickAction
         *   - only returns true for enchanted items by default
         * - otherwise, replaces the held item
         */
        return inventory.getSuitableHotbarSlot();
    }

    public static List<ToolboxItemReference> findToolboxesInInventory(Player player, int limit) {
        Inventory inventory = player.getInventory();
        List<ToolboxItemReference> result = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (AllTags.AllItemTags.TOOLBOXES.matches(stack)) {
                result.add(ToolboxItemReference.of(slot, stack));
            }
        }
        result.sort(Comparator.comparing(ToolboxItemReference::uuid));
        return result.size() <= limit
                ? List.copyOf(result)
                : List.copyOf(result.subList(0, limit));
    }

    public static Optional<UUID> getToolboxUUID(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.hasUUID("UniqueId")) {
            return Optional.empty();
        }
        return Optional.of(tag.getUUID("UniqueId"));
    }
}
