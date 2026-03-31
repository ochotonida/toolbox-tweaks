package toolboxtweaks.mixin.toolbox.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.equipment.toolbox.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import toolboxtweaks.mixin.toolbox.ToolboxBlockEntityAccessor;
import toolboxtweaks.toolbox.ToolboxHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ToolboxHandlerClient.class)
public class ToolboxHandlerClientMixin {

    // Always open the radial menu when there is at least one inaccessible toolbox
    @ModifyExpressionValue(method = "onKeyInput", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"), remap = false)
    private static boolean modifyIsEmpty(boolean original) {
        return original && !ToolboxHelper.hasInaccessibleToolbox(Minecraft.getInstance().player);
    }

    // Open the SELECT_BOX menu instead of SELECT_ITEM when there is at least one inaccessible toolbox
    @ModifyExpressionValue(method = "onKeyInput", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"), remap = false)
    private static int modifyToolboxSizeCheck(int original) {
        if (original == 1 && ToolboxHelper.hasInaccessibleToolbox(Minecraft.getInstance().player)) {
            return 2;
        }
        return original;
    }

    // When using pick block, switch to a more suitable hotbar slot before retrieving the item from a toolbox
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
            // switch to a matching hotbar slot if the picked item is already in the hotbar
            inventory.selected = matchingSlot;
        } else {
            // switch to a more suitable hotbar slot before replacing the item in it
            inventory.selected = ToolboxHelper.getSuitableHotbarSlot(inventory);
        }

        /* Send the ToolboxEquipPacket
         * - moves the item that's currently in the selected slot into the inventory (if needed)
         * - moves the picked item from the toolbox into the selected hotbar slot
         */
        return original.call(toolboxPos, slot, inventory.selected);
    }
}
