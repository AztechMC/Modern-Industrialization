package aztech.modern_industrialization.rei.forgehammer_recipe;

import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreen;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.impl.RenderingEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ForgeHammerRecipeCategory implements RecipeCategory<ForgeHammerRecipeDisplay> {
    private final Identifier id;
    private final boolean isHammer;

    public ForgeHammerRecipeCategory(MachineRecipeType type, boolean isHammer) {
        this.id = type.getId();
        this.isHammer = isHammer;
    }

    @Override
    public @NotNull Identifier getIdentifier() {
        return id;
    }

    @Override
    public @NotNull String getCategoryName() {
        return I18n.translate(id.toString());
    }

    @NotNull
    @Override
    public EntryStack getLogo() {
        return new RenderingEntry() {
            @Override
            public void render(MatrixStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                MinecraftClient.getInstance().getTextureManager().bindTexture(ForgeHammerScreen.FORGE_HAMMER_GUI);
                drawTexture(matrices, bounds.x+1, bounds.y+1, 206, isHammer ? 0 : 15, 15, 15);
            }
        };
    }

    @Override
    public @NotNull List<Widget> setupDisplay(ForgeHammerRecipeDisplay recipeDisplay, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.getCenterY() - 13);
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createArrow(new Point(startPoint.x + 27, startPoint.y + 4)));
        widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 61, startPoint.y + 5)));
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 4, startPoint.y + 5)).entries(recipeDisplay.getInputEntries().get(0)).markInput());
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 61, startPoint.y + 5)).entries(recipeDisplay.getResultingEntries().get(0)).disableBackground().markInput());
        return widgets;
    }
}
