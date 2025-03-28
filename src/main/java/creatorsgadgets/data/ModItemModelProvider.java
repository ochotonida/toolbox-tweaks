package creatorsgadgets.data;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;

import java.util.ArrayList;
import java.util.List;

public class ModItemModelProvider {

    public static void createCompassModel(DataGenContext<Item, ? extends Item> context, RegistrateItemModelProvider provider) {
        List<ModelFile> models = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            String s = (i > 9 ? "" : "0") + i;
            models.add(provider.getBuilder("gauge_compass_" + s)
                    .parent(new ModelFile.UncheckedModelFile("item/generated"))
                    .texture("layer1", "item/compass_needle_" + s)
                    .texture("layer0", "item/gauge_compass_base"));
        }
        ItemModelBuilder builder = provider.getBuilder("gauge_compass")
                .parent(models.get(0))
                .override()
                .predicate(ResourceLocation.parse("angle"), 0)
                .model(models.get(0))
                .end();
        for (int i = 0; i < 32; i++) {
            builder.override()
                    .predicate(ResourceLocation.parse("angle"),  (2 * i + 1) / 64F)
                    .model(models.get((i + 1) % 32))
                    .end();
        }
    }
}
