package creatorsgadgets.data;

import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import creatorsgadgets.CreatorsGadgets;
import creatorsgadgets.integration.ModCompat;
import creatorsgadgets.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTagProvider {

    public static void addGenerators() {
        CreatorsGadgets.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, ModTagProvider::genItemTags);
    }

    private static void genItemTags(RegistrateTagsProvider<Item> provIn) {
        TagGen.CreateTagsProvider<Item> prov = new TagGen.CreateTagsProvider<>(provIn, Item::builtInRegistryHolder);

        prov.tag(key(ModCompat.CURIOS, "charm")).add(
                ModItems.TOOLBOX_RADAR.get(),
                ModItems.TOOLBOX_RESONATOR.get()
        );
        prov.tag(key(ModCompat.CURIOS, "head")).add(
                ModItems.TOOLBOX_RADAR.get(),
                ModItems.TOOLBOX_RESONATOR.get()
        );
        prov.tag(key(ModCompat.CURIOS, "bracelet")).add(
                ModItems.PECULIAR_WATCH.get()
        );
        prov.tag(key(ModCompat.CURIOS, "hands")).add(
                ModItems.PECULIAR_WATCH.get()
        );
    }

    private static TagKey<Item> key(String namespace, String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
