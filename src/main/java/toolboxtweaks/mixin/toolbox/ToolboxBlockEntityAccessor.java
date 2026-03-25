package toolboxtweaks.mixin.toolbox;

import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ToolboxBlockEntity.class)
public interface ToolboxBlockEntityAccessor {

    @Accessor
    ToolboxInventory getInventory();
}
