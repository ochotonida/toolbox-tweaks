package creatorsgadgets.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import creatorsgadgets.CreatorsGadgets;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class WatchItemRenderer extends CustomRenderedItemModelRenderer {

    protected static final PartialModel HOUR_HAND = PartialModel.of(CreatorsGadgets.id("item/watch/hour_hand"));
    protected static final PartialModel MINUTE_HAND = PartialModel.of(CreatorsGadgets.id("item/watch/minute_hand"));

    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        boolean isNatural = player.level().dimensionType().natural();
        float dayTime = (int) (((player.level().getDayTime() + AnimationTickHolder.getPartialTicks()) * (isNatural ? 1 : 24)) % 24000);
        float hours = (dayTime / 1000 + 6) % 24;
        float d = 0.975f;
        hours = (int) hours + (hours % 1 > d ? ((hours - d) % 1) / (1 - d) : 0);
        float minutes = (dayTime % 1000) * 60 / 1000;
        renderer.render(model.getOriginalModel(), light);
        ms.pushPose();
        ms.mulPose(Axis.YN.rotationDegrees(hours / 24f * 720));
        renderer.render(HOUR_HAND.get(), light);
        ms.popPose();
        ms.pushPose();
        ms.mulPose(Axis.YN.rotationDegrees(minutes / 60f * 360));
        renderer.render(MINUTE_HAND.get(), light);
        ms.popPose();
    }
}
