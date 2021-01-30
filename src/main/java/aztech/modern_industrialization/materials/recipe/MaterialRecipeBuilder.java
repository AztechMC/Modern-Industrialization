package aztech.modern_industrialization.materials.recipe;

public interface MaterialRecipeBuilder {
    String getRecipeId();
    void cancel();

    /**
     * @deprecated don't call, let the MaterialBuilder do it
     */
    @Deprecated
    void save();
}
