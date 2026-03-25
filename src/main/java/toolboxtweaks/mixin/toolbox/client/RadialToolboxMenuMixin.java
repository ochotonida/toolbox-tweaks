package toolboxtweaks.mixin.toolbox.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.toolbox.RadialToolboxMenu;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandler;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import toolboxtweaks.toolbox.ToolboxHelper;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = RadialToolboxMenu.class)
public abstract class RadialToolboxMenuMixin extends AbstractSimiScreen {

    @Shadow(remap = false)
    private RadialToolboxMenu.State state;
    @Shadow(remap = false)
    private int ticksOpen;
    @Shadow(remap = false)
    private List<ToolboxBlockEntity> toolboxes;

    @Shadow(remap = false)
    private int hoveredSlot;
    @Shadow(remap = false)
    private int scrollSlot;
    @Shadow(remap = false)
    private boolean scrollMode;

    @Unique
    private List<ToolboxBlockEntity> creatorsGadgets$distantToolboxes = List.of();
    @Unique
    @Nullable
    private ToolboxBlockEntity creatorsGadgets$detachedBox;

    @Inject(method = "<init>", remap = false, at = @At("TAIL"))
    private void init(List<ToolboxBlockEntity> toolboxes, RadialToolboxMenu.State state, ToolboxBlockEntity selectedBox, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            if (state == RadialToolboxMenu.State.SELECT_BOX && toolboxes.size() < 8) {
                creatorsGadgets$distantToolboxes = ToolboxHelper.getNearestOutsideRange(player.level(), player, 8 - toolboxes.size());
            } else if (state == RadialToolboxMenu.State.DETACH) {
                creatorsGadgets$detachedBox = ToolboxHelper.getBoxForSelectedItem(player);
            }
        }
    }

    @Inject(method = "renderWindow", remap = false, at = @At("TAIL"))
    public void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        float fade = Mth.clamp(((float)ticksOpen + AnimationTickHolder.getPartialTicks()) / 10F, 0.002F, 1F);
        Component tooltip = null;

        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(width / 2F, height / 2F, 0);
        int slot;
        if (state == RadialToolboxMenu.State.DETACH) {
            creatorsgadgets$renderToolboxDistance(graphics, creatorsGadgets$detachedBox, fade, true);
        } else if (state == RadialToolboxMenu.State.SELECT_BOX) {
            for (slot = 0; slot < 8; ++slot) {
                ms.pushPose();
                TransformStack.of(ms)
                        .rotateZDegrees(slot * 45 - 45)
                        .translate(0, -40 + 10 * (1 - fade) * (1 - fade), 0)
                        .rotateZDegrees(-slot * 45 + 45);
                if (slot < toolboxes.size()) {
                    ToolboxBlockEntity toolbox = toolboxes.get(slot);
                    creatorsgadgets$renderToolboxDistance(graphics, toolbox, fade, false);
                } else if (slot - toolboxes.size() < creatorsGadgets$distantToolboxes.size()){
                    ToolboxBlockEntity toolbox = creatorsGadgets$distantToolboxes.get(slot - toolboxes.size());
                    Component text = creatorsGadgets$renderDistantToolbox(graphics, slot, toolbox, fade);
                    if (text != null) {
                        tooltip = text;
                    }
                }
                ms.popPose();
            }
        }
        ms.popPose();

        if (tooltip != null) {
            creatorsGadgets$renderTooltip(graphics, tooltip, fade);
        }
    }

    @Unique
    private Component creatorsGadgets$renderDistantToolbox(GuiGraphics graphics, int slot, ToolboxBlockEntity toolbox, float fade) {
        AllGuiTextures.TOOLBELT_INACTIVE_SLOT.render(graphics, -12, -12);
        GuiGameElement.of(AllBlocks.TOOLBOXES.get(toolbox.getColor())
                        .asStack())
                .at(-9, -9)
                .render(graphics);
        creatorsgadgets$renderToolboxDistance(graphics, toolbox, fade, true);
        if (slot == (scrollMode ? scrollSlot : hoveredSlot)) {
            return toolbox.getDisplayName();
        }
        return null;
    }

    @Unique
    private void creatorsgadgets$renderToolboxDistance(GuiGraphics graphics, ToolboxBlockEntity toolbox, float fade, boolean distant) {
        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(8, -0.5, 150);
        Player player = Minecraft.getInstance().player;
        int distance = 100;
        if (player != null && toolbox != null && player.level() == toolbox.getLevel()) {
            distance = Math.round(Mth.sqrt((float) ToolboxHandler.distance(player.position(), toolbox.getBlockPos())));
        }
        float maxRange = (float) Math.min(ToolboxHandler.getMaxRange(player), 99);
        float d = Math.min(distance, maxRange) / maxRange;
        int color = Mth.hsvToRgb(Mth.lerp(d, 1/3F, 0.03F), Mth.lerp(d, 0.5F, 0.7F), 1);
        if (distant) {
            color = Mth.hsvToRgb(0, 0.7F, 1);
        }
        Style style = Style.EMPTY.withColor(color);
        Component text = Component
                .literal(Math.min(99, distance) + (distance > 99 ? "+" : "m"))
                .withStyle(style);
        creatorsGadgets$renderText(graphics, text, fade, -this.font.width(text), 0, true);
        ms.popPose();
    }

    @Unique
    private void creatorsGadgets$renderTooltip(GuiGraphics graphics, Component text, float fade) {
        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate((float) (width / 2), (float) (height - 68), 0F);
        float textWidth = font.width(text);
        creatorsGadgets$renderText(graphics, text, fade, Math.round(-textWidth / 2), -4, false);
        ms.popPose();
    }

    @Unique
    private void creatorsGadgets$renderText(GuiGraphics graphics, Component text, float fade, int dx, int dy, boolean shadow) {
        int a = Math.min(255, (int) (fade * 255F));
        if (a > 8) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            int color = 0x00FFFFFF | (a << 24 & 0xFF000000);
            graphics.drawString(font, text, dx, dy, color, shadow);
            RenderSystem.disableBlend();
        }
    }
}
