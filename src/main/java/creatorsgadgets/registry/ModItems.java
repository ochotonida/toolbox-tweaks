package creatorsgadgets.registry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import creatorsgadgets.CreatorsGadgets;
import creatorsgadgets.data.ModItemModelProvider;
import creatorsgadgets.item.EquipableItem;
import creatorsgadgets.item.WatchItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;

public class ModItems {

    private static final CreateRegistrate REGISTRATE = CreatorsGadgets.REGISTRATE;

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreatorsGadgets.MOD_ID);

    @SuppressWarnings("unused")
    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(REGISTRATE.addLang("itemGroup", CreatorsGadgets.id("main"), "Creator's Gadgets"))
                    .icon(() -> new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(CreatorsGadgets.id("toolbox_radar")))))
                    .displayItems((parameters, output) -> REGISTRATE.getAll(Registries.ITEM).forEach(entry -> output.accept(entry.get())))
                    .build());

    public static final ItemEntry<EquipableItem> TOOLBOX_RADAR = REGISTRATE.item("toolbox_radar", p -> new EquipableItem(p, EquipmentSlot.HEAD))
            .model((i, t) -> { })
            .properties(p -> p.stacksTo(1))
            .register();
    public static final ItemEntry<EquipableItem> TOOLBOX_RESONATOR = REGISTRATE.item("toolbox_resonator", p -> new EquipableItem(p, EquipmentSlot.HEAD))
            .model((i, t) -> { })
            .properties(p -> p.stacksTo(1))
            .register();
    public static final ItemEntry<? extends Item> GAUGE_COMPASS = REGISTRATE.item("gauge_compass", CompassItem::new)
            .model(ModItemModelProvider::createCompassModel)
            .properties(p -> p.stacksTo(1))
            .register();
    public static final ItemEntry<WatchItem> PECULIAR_WATCH = REGISTRATE.item("peculiar_watch", WatchItem::new)
            .properties(p -> p.stacksTo(1))
            .model((i, t) -> { })
            .register();

    public static void register() {

    }
}
