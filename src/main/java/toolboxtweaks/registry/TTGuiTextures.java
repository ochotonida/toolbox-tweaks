package toolboxtweaks.registry;

import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import toolboxtweaks.ToolboxTweaks;

public enum TTGuiTextures implements ScreenElement, TextureSheetSegment {
    INVENTORY_TOOLBOX_SLOT("inventory_toolbox_slot", 22, 22);

    public final ResourceLocation id;
    private final int width;
    private final int height;

    TTGuiTextures(String path, int width, int height) {
        this(ToolboxTweaks.MOD_ID, path, width, height);
    }

    TTGuiTextures(String namespace, String path, int width, int height) {
        this.id = ResourceLocation.fromNamespaceAndPath(namespace, "textures/gui/%s.png".formatted(path));
        this.width = width;
        this.height = height;
    }

    @Override
    public ResourceLocation getLocation() {
        return id;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(id, x, y, 0, 0, width, height, width, height);
    }

    @Override
    public int getStartX() {
        return 0;
    }

    @Override
    public int getStartY() {
        return 0;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
