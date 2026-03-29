package toolboxtweaks.mixin.toolbox.client;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.equipment.toolbox.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import toolboxtweaks.mixin.toolbox.ToolboxBlockEntityAccessor;
import toolboxtweaks.toolbox.ToolboxHelper;
import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

@Mixin(ToolboxHandlerClient.class)
public class ToolboxHandlerClientMixin {

    @Inject(method = "onKeyInput", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"), remap = false)
    private static void forceOpenSelectBox(int key, boolean pressed, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        List<ToolboxBlockEntity> toolboxes = ToolboxHandler.getNearest(player.level(), player, 8);
        toolboxes.sort(Comparator.comparing(ToolboxBlockEntity::getUniqueId));
        if (toolboxes.isEmpty() && toolboxTweaks$hasToolboxesOutsideRange(player)) {
            ScreenOpener.open(new RadialToolboxMenu(ImmutableList.of(), RadialToolboxMenu.State.SELECT_BOX, null));
        }
    }

    @ModifyExpressionValue(method = "onKeyInput", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"), remap = false)
    private static int modifyToolboxSizeCheck(int original) {
        if (original == 1 && toolboxTweaks$hasToolboxesOutsideRange(Minecraft.getInstance().player)) {
            return 2;
        }
        return original;
    }

    @WrapOperation(method = "onPickItem", at = @At(value = "NEW", target = "(Lnet/minecraft/core/BlockPos;II)Lcom/simibubi/create/content/equipment/toolbox/ToolboxEquipPacket;"), remap = false)
    private static ToolboxEquipPacket modifyPickItemSlot(BlockPos toolboxPos, int slot, int hotbarSlot, Operation<ToolboxEquipPacket> original) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            throw new IllegalStateException("Client player is null");
        }

        Inventory inventory = player.getInventory();
        ToolboxBlockEntity toolbox = ToolboxHandler.toolboxes.get(player.level()).get(toolboxPos);
        ItemStack inSlot = ((ToolboxBlockEntityAccessor) toolbox).getInventory().takeFromCompartment(1, slot, true);

        int matchingSlot = inventory.findSlotMatchingItem(inSlot);
        if (Inventory.isHotbarSlot(matchingSlot)) {
            inventory.selected = matchingSlot;
        } else {
            inventory.selected = toolboxTweaks$getSuitableHotbarSlot(inventory);
        }

        return original.call(toolboxPos, slot, inventory.selected);
    }

    @Unique
    private static int toolboxTweaks$getSuitableHotbarSlot(Inventory inventory) {
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
                    && toolboxTweaks$isLinkedWithToolbox(inventory.player, slot)
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

    @Unique
    private static boolean toolboxTweaks$isLinkedWithToolbox(Player player, int slot) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains("CreateToolboxData")) {
            return false;
        }
        CompoundTag toolboxData = player.getPersistentData().getCompound("CreateToolboxData");
        return toolboxData.contains(String.valueOf(slot));

    }

    @Unique
    private static boolean toolboxTweaks$hasToolboxesOutsideRange(Player player) {
        return !ToolboxHelper.getNearestOutsideRange(player.level(), player, 1).isEmpty();
    }
}
