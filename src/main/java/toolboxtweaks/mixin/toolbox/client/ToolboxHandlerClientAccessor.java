package toolboxtweaks.mixin.toolbox.client;

import com.simibubi.create.content.equipment.toolbox.ToolboxHandlerClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ToolboxHandlerClient.class)
public interface ToolboxHandlerClientAccessor {

    @Accessor(value = "COOLDOWN")
    static void setCooldown(int cooldown) {

    }
}
