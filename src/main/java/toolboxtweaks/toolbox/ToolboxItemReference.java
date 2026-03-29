package toolboxtweaks.toolbox;

import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record ToolboxItemReference(int slot, ItemStack stack, UUID uuid) {

    public static ToolboxItemReference of(int slot, ItemStack stack) {
        UUID uuid = ToolboxHelper.getToolboxUUID(stack).orElse(new UUID(0, 0));
        return new ToolboxItemReference(slot, stack, uuid);
    }
}
