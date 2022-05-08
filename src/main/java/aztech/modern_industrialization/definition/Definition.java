package aztech.modern_industrialization.definition;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import java.util.Objects;

public abstract class Definition<T> {

    private final ResourceLocation id;
    private final String englishName;

    public String getEnglishName() {
        return englishName;
    }

    public ResourceLocation getId() {
        return id;
    }

    public abstract String getTranslationKey();

    public Definition(ResourceLocation id, String englishName) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(englishName, "englishName");
        this.id = id;
        this.englishName = englishName;
    }

}
