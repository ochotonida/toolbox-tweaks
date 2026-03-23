package creatorsgadgets.item;

import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import creatorsgadgets.client.WatchItemRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class WatchItem extends Item {

    public WatchItem(Properties properties) {
        super(properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new WatchItemRenderer()));
    }
}
