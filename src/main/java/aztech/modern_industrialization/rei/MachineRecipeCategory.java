package aztech.modern_industrialization.rei;

import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.SteamMachineFactory;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.widgets.Slot;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.widget.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MachineRecipeCategory implements RecipeCategory<MachineRecipeDisplay> {
    private final Identifier id;
    private final MachineFactory factory;
    private final EntryStack logo;

    public MachineRecipeCategory(MachineRecipeType type, MachineFactory factory, EntryStack logo) {
        this.id = type.getId();
        this.factory = factory;
        this.logo = logo;
    }

    @Override
    public @NotNull Identifier getIdentifier() {
        return id;
    }

    @Override
    public @NotNull String getCategoryName() {
        return I18n.translate(id.toString());
    }

    @Override
    public @NotNull EntryStack getLogo() {
        return logo;
    }

    @FunctionalInterface
    private interface SlotDrawer {
        void drawSlots(Stream<List<EntryStack>> entries, int[] slots, boolean input);
    }

    @Override
    public @NotNull List<Widget> setupDisplay(MachineRecipeDisplay recipeDisplay, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        int x = 1000, y = 1000, X = 0, Y = 0;
        for(int i = 0; i < factory.getSlots(); i++) {
            if(i == factory.getInputSlots() && factory instanceof SteamMachineFactory) {
                continue;
            }
            x = Math.min(x, factory.getSlotPosX(i));
            X = Math.max(X, factory.getSlotPosX(i) + 16);
            y = Math.min(y, factory.getSlotPosY(i));
            Y = Math.max(Y, factory.getSlotPosY(i) + 16);
        }

        int xoffset = bounds.x + (bounds.width - X + x) / 2 - x;
        int yoffset = bounds.y + (bounds.height - Y + y) / 2 - y;

        SlotDrawer drawer = (entryStream, slots, input) -> {
            List<List<EntryStack>> entries = entryStream.collect(Collectors.toList());
            for(int i = 0; i < slots.length; ++i) {
                List<EntryStack> stack = i < entries.size() ? entries.get(i) : Collections.emptyList();
                Slot widget = Widgets.createSlot(new Point(xoffset + factory.getSlotPosX(slots[i]), yoffset + factory.getSlotPosY(slots[i]))).entries(stack);
                if(input) {
                    widget.markInput();
                } else {
                    widget.markOutput();
                }
                widgets.add(widget);
            }
        };

        drawer.drawSlots(recipeDisplay.getItemInputs(), factory.getInputIndices(), true);
        drawer.drawSlots(recipeDisplay.getFluidInputs(), factory.getFluidInputIndices(), true);
        drawer.drawSlots(recipeDisplay.getItemOutputs(), factory.getOutputIndices(), false);
        drawer.drawSlots(recipeDisplay.getFluidOutputs(), factory.getFluidOutputIndices(), false);

        if(factory.hasProgressBar()) {
            double recipeMillis = recipeDisplay.getSeconds() * 1000;
            widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                MinecraftClient.getInstance().getTextureManager().bindTexture(factory.getBackgroundIdentifier());
                double progress = (System.currentTimeMillis() / recipeMillis) % 1.0;
                int sx = factory.getProgressBarSizeX();
                int sy = factory.getProgressBarSizeY();

                int px = xoffset + factory.getProgressBarDrawX();
                int py = yoffset + factory.getProgressBarDrawY();

                int u = factory.getProgressBarX();
                int v = factory.getProgressBarY();

                // Base arrow
                helper.drawTexture(matrices, px, py, factory.getProgressBarDrawX(), factory.getProgressBarDrawY(), sx, sy);
                // Overlay
                if (factory.isProgressBarHorizontal()) {
                    int progressPixel = (int) (progress * sx);
                    helper.drawTexture(matrices, px, py, u, v, progressPixel, sy);
                } else if (factory.isProgressBarFlipped()) {
                    int progressPixel = (int) ((1 - progress) * sy);
                    helper.drawTexture(matrices, px, py + progressPixel, u, v + progressPixel, sx, sy - progressPixel);
                } else {
                    int progressPixel = (int) (progress * sy);
                    helper.drawTexture(matrices, px, py, u, v, sx, progressPixel);
                }
            }));
        }

        return widgets;
    }
}
