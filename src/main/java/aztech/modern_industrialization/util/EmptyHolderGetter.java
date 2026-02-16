package aztech.modern_industrialization.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.Optional;

public final class EmptyHolderGetter<T> implements HolderGetter<T> {
    private static final EmptyHolderGetter<?> INSTANCE = new EmptyHolderGetter<>();

    public static <T> EmptyHolderGetter<T> getInstance() {
        return (EmptyHolderGetter<T>) INSTANCE;
    }

    private EmptyHolderGetter() {}

    @Override
    public Optional<Holder.Reference<T>> get(ResourceKey<T> id) {
        return Optional.empty();
    }

    @Override
    public Optional<HolderSet.Named<T>> get(TagKey<T> id) {
        return Optional.empty();
    }
}
