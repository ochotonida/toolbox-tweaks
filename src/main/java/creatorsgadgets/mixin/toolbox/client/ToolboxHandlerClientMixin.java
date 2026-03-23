package creatorsgadgets.mixin.toolbox.client;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.equipment.toolbox.RadialToolboxMenu;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandler;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandlerClient;
import creatorsgadgets.toolbox.ToolboxHelper;
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
        if (toolboxes.isEmpty() && creatorsGadgets$hasToolboxesOutsideRange(player)) {
            ScreenOpener.open(new RadialToolboxMenu(ImmutableList.of(), RadialToolboxMenu.State.SELECT_BOX, null));
        }
    }

    @ModifyExpressionValue(method = "onKeyInput", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"), remap = false)
    private static int modifyToolboxSizeCheck(int original) {
        if (original == 1 && creatorsGadgets$hasToolboxesOutsideRange(Minecraft.getInstance().player)) {
            return 2;
        }
        return original;
    }

    @Unique
    private static boolean creatorsGadgets$hasToolboxesOutsideRange(Player player) {
        return !ToolboxHelper.getNearestOutsideRange(player.level(), player, 1).isEmpty();
    }
}
