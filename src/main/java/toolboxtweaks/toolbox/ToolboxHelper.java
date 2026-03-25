package toolboxtweaks.toolbox;

import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;
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
        WeakHashMap<BlockPos, ToolboxBlockEntity> var10001 = ToolboxHandler.toolboxes.get(world);
        Objects.requireNonNull(var10001);
        return positions.map(var10001::get).filter(ToolboxBlockEntity::isFullyInitialized).collect(Collectors.toList());
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
}
