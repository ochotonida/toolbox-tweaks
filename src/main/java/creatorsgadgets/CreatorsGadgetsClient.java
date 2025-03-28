package creatorsgadgets;

import creatorsgadgets.integration.curios.CuriosIntegrationClient;
import creatorsgadgets.registry.ModItems;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CreatorsGadgetsClient {

    public CreatorsGadgetsClient(IEventBus modEventBus) {
        modEventBus.addListener(this::clientSetup);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        CuriosIntegrationClient.setup();
        event.enqueueWork(() -> ItemProperties.register(
                ModItems.GAUGE_COMPASS.get(),
                ResourceLocation.parse("angle"),
                new CompassItemPropertyFunction((level, item, entity) -> null)
        ));
    }
}
