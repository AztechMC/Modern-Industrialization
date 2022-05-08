package aztech.modern_industrialization.definition;

import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ItemDefinition<T extends Item> extends Definition<T> implements ItemLike {

    private final T item;

    public final BiConsumer<Item, ItemModelGenerators> modelGenerator;
    private Consumer<Item> onItemRegistrationEvent;

    public ItemDefinition(String englishName, ResourceLocation id, T item,
                          BiConsumer<Item, ItemModelGenerators> modelGenerator) {
        super(id, englishName);
        this.item = item;
        this.modelGenerator = modelGenerator;
        this.onItemRegistrationEvent = null;
    }

    public ItemDefinition<T> withItemRegistrationEvent(Consumer<Item> onItemRegistrationEvent) {
        this.onItemRegistrationEvent = onItemRegistrationEvent;
        return this;
    }

    public void onRegister() {
        if (this.onItemRegistrationEvent != null) {
            this.onItemRegistrationEvent.accept(item);
        }
    }

    public ItemStack stack() {
        return stack(1);
    }

    public ItemStack stack(int stackSize) {
        return new ItemStack(item, stackSize);
    }

    @Override
    public T asItem() {
        return item;
    }

    @Override
    public String getTranslationKey() {
        return item.getDescriptionId();
    }


}
