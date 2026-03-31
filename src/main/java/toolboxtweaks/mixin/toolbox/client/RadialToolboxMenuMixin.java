package toolboxtweaks.mixin.toolbox.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.toolbox.RadialToolboxMenu;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandler;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import toolboxtweaks.toolbox.ToolboxHelper;
import toolboxtweaks.toolbox.ToolboxItemReference;

import java.util.List;
import java.util.Objects;

@Mixin(value = RadialToolboxMenu.class)
public abstract class RadialToolboxMenuMixin extends AbstractSimiScreen {

    @Unique
    private static final int toolboxTweaks$NUM_COMPARTMENTS = 8;

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
    private List<ToolboxItemReference> toolboxTweaks$inventoryToolboxes = List.of();
    @Unique
    private List<ToolboxBlockEntity> toolboxTweaks$distantToolboxes = List.of();
    @Unique
    @Nullable
    private ToolboxBlockEntity toolboxTweaks$detachedBox;

    @Inject(method = "<init>", remap = false, at = @At("TAIL"))
    private void init(List<ToolboxBlockEntity> toolboxes, RadialToolboxMenu.State state, ToolboxBlockEntity selectedBox, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            throw new IllegalStateException("Client player is null");
        }

        int availableSlots = toolboxTweaks$NUM_COMPARTMENTS - toolboxes.size();
        if (availableSlots > 0) {
            toolboxTweaks$distantToolboxes = ToolboxHelper.getNearestOutsideRange(player.level(), player, availableSlots);
            availableSlots -= toolboxTweaks$distantToolboxes.size();
        }
        if (availableSlots > 0) {
            toolboxTweaks$inventoryToolboxes = ToolboxHelper.findToolboxesInInventory(player, availableSlots);
        }
        toolboxTweaks$detachedBox = ToolboxHelper.getBoxForSelectedItem(player);
    }

    @Inject(method = "renderWindow", remap = false, at = @At("TAIL"))
    public void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        float fade = Mth.clamp(((float) ticksOpen + AnimationTickHolder.getPartialTicks()) / 10F, 0.002F, 1F);

        PoseStack ms = graphics.pose();
        ms.pushPose();

        if (state == RadialToolboxMenu.State.DETACH) {
            ms.translate(width / 2F, height / 2F, 0);
            toolboxTweaks$renderToolboxDistance(graphics, toolboxTweaks$detachedBox, fade, true);
        } else if (state == RadialToolboxMenu.State.SELECT_BOX) {
            Component tooltip = null;
            for (int slot = 0; slot < 8; slot++) {
                ms.pushPose();
                ms.translate(width / 2F, height / 2F, 0);
                TransformStack.of(ms)
                        .rotateZDegrees(slot * 45 - 45)
                        .translate(0, -40 + 10 * (1 - fade) * (1 - fade), 0)
                        .rotateZDegrees(-slot * 45 + 45);
                Component currentSlotTooltip = toolboxTweaks$renderSlot(graphics, slot, fade);
                if (currentSlotTooltip != null) {
                    tooltip = currentSlotTooltip;
                }
                ms.popPose();
            }
            if (tooltip != null) {
                toolboxTweaks$renderTooltip(graphics, tooltip, fade);
            }
        }

        ms.popPose();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
        Player player = Minecraft.getInstance().player;
        if (button != 0 || player == null) {
            return;
        }

        int selected = scrollMode ? scrollSlot : hoveredSlot;
        int invIndex = selected - toolboxes.size() - toolboxTweaks$distantToolboxes.size();
        if (invIndex < 0 || invIndex >= toolboxTweaks$inventoryToolboxes.size()) {
            return;
        }

        int inventorySlot = toolboxTweaks$inventoryToolboxes.get(invIndex).slot();
        if (inventorySlot < 9) { // toolbox is already in hotbar
            player.getInventory().selected = inventorySlot;
        } else { // move toolbox to hotbar
            player.getInventory().selected = ToolboxHelper.getSuitableHotbarSlot(player.getInventory());
            Objects.requireNonNull(Minecraft.getInstance().getConnection())
                    .send(new ServerboundPickItemPacket(inventorySlot));
        }

        onClose();
        ToolboxHandlerClientAccessor.setCooldown(2);
        cir.setReturnValue(true); // Early return intended!
    }

    @Definition(id = "toolboxes", remap = false, field = "Lcom/simibubi/create/content/equipment/toolbox/RadialToolboxMenu;toolboxes:Ljava/util/List;")
    @Definition(id = "size", remap = false, method = "Ljava/util/List;size()I")
    @Expression("this.toolboxes.size() > 1")
    @ModifyExpressionValue(
            method = "mouseClicked",
            at = @At(value = "MIXINEXTRAS:EXPRESSION")
    )
    private boolean shouldReturnToBoxSelection(boolean original) {
        return original || toolboxes.size() + toolboxTweaks$distantToolboxes.size() + toolboxTweaks$inventoryToolboxes.size() > 1;
    }

    @Unique
    private Component toolboxTweaks$renderSlot(GuiGraphics graphics, int slot, float fade) {
        int distantStart = toolboxes.size();
        int inventoryStart = distantStart + toolboxTweaks$distantToolboxes.size();

        if (slot < distantStart) {
            toolboxTweaks$renderToolboxDistance(graphics, toolboxes.get(slot), fade, false);
        } else if (slot < inventoryStart) {
            return toolboxTweaks$renderDistantToolbox(graphics, slot, toolboxTweaks$distantToolboxes.get(slot - distantStart), fade);
        } else if (slot - inventoryStart < toolboxTweaks$inventoryToolboxes.size()) {
            return toolboxTweaks$renderInventoryToolbox(graphics, slot, toolboxTweaks$inventoryToolboxes.get(slot - inventoryStart).stack());
        }
        return null;
    }

    @Unique
    private Component toolboxTweaks$renderInventoryToolbox(GuiGraphics graphics, int slot, ItemStack stack) {
        boolean isSelected = slot == (scrollMode ? scrollSlot : hoveredSlot);
        AllGuiTextures.TOOLBELT_SLOT.render(graphics, -12, -12); // TODO custom texture
        if (isSelected) {
            AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(graphics, -13, -13);
        }
        GuiGameElement.of(stack).at(-9, -9).render(graphics);
        if (isSelected) {
            return stack.getHoverName();
        }
        return null;
    }

    @Unique
    private Component toolboxTweaks$renderDistantToolbox(GuiGraphics graphics, int slot, ToolboxBlockEntity toolbox, float fade) {
        AllGuiTextures.TOOLBELT_INACTIVE_SLOT.render(graphics, -12, -12);
        GuiGameElement.of(AllBlocks.TOOLBOXES.get(toolbox.getColor())
                        .asStack())
                .at(-9, -9)
                .render(graphics);
        toolboxTweaks$renderToolboxDistance(graphics, toolbox, fade, true);
        if (slot == (scrollMode ? scrollSlot : hoveredSlot)) {
            return toolbox.getDisplayName();
        }
        return null;
    }

    @Unique
    private void toolboxTweaks$renderToolboxDistance(GuiGraphics graphics, ToolboxBlockEntity toolbox, float fade, boolean distant) {
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
        toolboxTweaks$renderText(graphics, text, fade, -this.font.width(text), 0, true);
        ms.popPose();
    }

    @Unique
    private void toolboxTweaks$renderTooltip(GuiGraphics graphics, Component text, float fade) {
        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate((float) (width / 2), (float) (height - 68), 0F);
        float textWidth = font.width(text);
        toolboxTweaks$renderText(graphics, text, fade, Math.round(-textWidth / 2), -4, false);
        ms.popPose();
    }

    @Unique
    private void toolboxTweaks$renderText(GuiGraphics graphics, Component text, float fade, int dx, int dy, boolean shadow) {
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
