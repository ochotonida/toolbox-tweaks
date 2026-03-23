package creatorsgadgets.data;

import com.tterrag.registrate.util.entry.ItemEntry;
import creatorsgadgets.registry.ModItems;

import java.util.function.BiConsumer;

public class ModTooltipProvider {

    private final BiConsumer<String, String> langConsumer;

    public ModTooltipProvider(BiConsumer<String, String> langConsumer) {
        this.langConsumer = langConsumer;
    }

    public void start() {
        String behaviour = "Displays the _distance_ to nearby _Toolboxes_. Shows Toolboxes _beyond_ their accessible range.";
        item(ModItems.TOOLBOX_RADAR)
                .summary("Helps you locate _Toolboxes_ you may have lost.")
                .condition("When holding the Toolbox Keybind", behaviour);
        item(ModItems.TOOLBOX_RESONATOR)
                .summary("Makes it easier to forget where you placed your _Toolbox_, and helps you find it again.")
                .condition("When Equipped", "Access _Toolboxes_ from a _greater distance_.")
                .condition("When holding the Toolbox Keybind", behaviour);
        item(ModItems.GAUGE_COMPASS)
                .summary("A compass that helps you locate _Factory Gauges_ that craft a specific item.")
                .condition("When Used on Block", "_Tunes_ this compass to the _logistics network_.")
                .condition("When R-Clicked", "Opens the _configuration interface_.");
        item(ModItems.PECULIAR_WATCH)
                .summary("Never lose track of time with this handy watch.");
    }

    private Builder item(ItemEntry<?> item) {
        return new Builder(item);
    }

    private class Builder {

        private final String item;
        private int conditionCount = 1;

        private Builder(ItemEntry<?> item) {
            this.item = item.getKey().location().toString().replace(':', '.');
        }

        private Builder summary(String summary) {
            langConsumer.accept("item.%s.tooltip.summary".formatted(item), summary);
            return this;
        }

        private Builder condition(String condition, String behaviour) {
            langConsumer.accept("item.%s.tooltip.condition%s".formatted(item, conditionCount), condition);
            langConsumer.accept("item.%s.tooltip.behaviour%s".formatted(item, conditionCount), behaviour);
            conditionCount += 1;
            return this;
        }
    }
}
