package aztech.modern_industrialization.mixin;

import aztech.modern_industrialization.mixin_impl.IngredientMatchingStacksAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Ingredient.class)
public abstract class IngredientMixin implements IngredientMatchingStacksAccessor {
    @Shadow
    private ItemStack[] matchingStacks;

    @Shadow
    protected abstract void cacheMatchingStacks();

    @Override
    public ItemStack[] modern_industrialization_getMatchingStacks() {
        this.cacheMatchingStacks();
        return this.matchingStacks;
    }
}
