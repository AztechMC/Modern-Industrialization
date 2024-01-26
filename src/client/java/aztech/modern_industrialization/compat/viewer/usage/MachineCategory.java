/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.compat.viewer.usage;

import static aztech.modern_industrialization.MITooltips.EU_PER_TICK_PARSER;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.compat.rei.machines.MachineCategoryParams;
import aztech.modern_industrialization.compat.viewer.abstraction.ViewerCategory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.machines.guicomponents.EnergyBarClient;
import aztech.modern_industrialization.machines.guicomponents.ProgressBarClient;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.init.MachineTier;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.RecipeConversions;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.util.TextHelper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;

public class MachineCategory extends ViewerCategory<RecipeHolder<MachineRecipe>> {
    public static MachineCategory create(MachineCategoryParams params) {
        int x = 1000, X = 0;
        int y = 1000, Y = 0;
        for (SlotPositions positions : new SlotPositions[] { params.itemInputs, params.itemOutputs, params.fluidInputs, params.fluidOutputs }) {
            for (int i = 0; i < positions.size(); ++i) {
                x = Math.min(x, positions.getX(i));
                X = Math.max(X, positions.getX(i) + 16);
                y = Math.min(y, positions.getY(i));
                Y = Math.max(Y, positions.getY(i) + 16);
            }
        }

        int width = Math.max(X - x + 15, 120);
        int height = Y - y + 25; // Room for text above

        return new MachineCategory(params, width, height);
    }

    private final MachineCategoryParams params;

    private MachineCategory(MachineCategoryParams params, int width, int height) {
        super((Class) RecipeHolder.class, new MIIdentifier(params.category),
                Component.translatable("rei_categories.modern_industrialization." + params.category),
                BuiltInRegistries.ITEM.get(params.workstations.get(0)).getDefaultInstance(), width, height);

        this.params = params;
    }

    @Override
    public void buildWorkstations(WorkstationConsumer consumer) {
        for (ResourceLocation workstation : params.workstations) {
            consumer.accept(BuiltInRegistries.ITEM.get(workstation));
        }
    }

    @Override
    public void buildRecipes(RecipeManager recipeManager, RegistryAccess registryAccess, Consumer<RecipeHolder<MachineRecipe>> consumer) {
        var machineRecipes = recipeManager.getRecipes().stream()
                .filter(r -> r.value() instanceof MachineRecipe)
                .map(r -> (RecipeHolder<MachineRecipe>) r)
                .toList();

        // regular recipes
        machineRecipes.stream()
                .filter(r -> params.recipePredicate.test(r.value()))
                .sorted(Comparator.comparing(RecipeHolder::id))
                .forEach(consumer);

        // converted recipes
        switch (params.category) {
        case "bronze_furnace" -> {
            recipeManager.getAllRecipesFor(RecipeType.SMELTING)
                    .stream()
                    .map(r -> RecipeConversions.ofSmelting(r, MIMachineRecipeTypes.FURNACE, registryAccess))
                    .forEach(consumer);
        }
        case "bronze_cutting_machine" -> {
            recipeManager.getAllRecipesFor(RecipeType.STONECUTTING)
                    .stream()
                    .map(r -> RecipeConversions.ofStonecutting(r, MIMachineRecipeTypes.CUTTING_MACHINE, registryAccess))
                    .forEach(consumer);
        }
        case "centrifuge" -> {
            ComposterBlock.COMPOSTABLES.keySet()
                    .stream()
                    .map(RecipeConversions::ofCompostable)
                    .filter(Objects::nonNull)
                    .forEach(consumer);
        }
        }
    }

    @Override
    public void buildLayout(RecipeHolder<MachineRecipe> recipe, LayoutBuilder builder) {
        addItemInputs(builder, recipe.value());
        addItemOutputs(builder, recipe.value());
        addFluidInputs(builder, recipe.value());
        addFluidOutputs(builder, recipe.value());
    }

    private void addItemInputs(LayoutBuilder builder, MachineRecipe recipe) {
        var offset = getOffset();

        var positions = params.itemInputs;
        for (int i = 0; i < positions.size(); i++) {
            var x = offset.x + positions.getX(i);
            var y = offset.y + positions.getY(i);
            var slot = builder.inputSlot(x, y);

            if (i < recipe.itemInputs.size()) {
                var input = recipe.itemInputs.get(i);
                slot.ingredient(input.ingredient, input.amount, input.probability);
            }
        }
    }

    private void addItemOutputs(LayoutBuilder builder, MachineRecipe recipe) {
        var offset = getOffset();

        var positions = params.itemOutputs;
        for (int i = 0; i < positions.size(); i++) {
            var x = offset.x + positions.getX(i);
            var y = offset.y + positions.getY(i);
            var slot = builder.outputSlot(x, y);

            if (i < recipe.itemOutputs.size()) {
                var output = recipe.itemOutputs.get(i);
                slot.item(output.getStack(), output.probability);
            }
        }
    }

    private void addFluidInputs(LayoutBuilder builder, MachineRecipe recipe) {
        var offset = getOffset();

        var positions = params.fluidInputs;
        for (int i = 0; i < positions.size(); i++) {
            var x = offset.x + positions.getX(i);
            var y = offset.y + positions.getY(i);
            var slot = builder.inputSlot(x, y);

            if (i < recipe.fluidInputs.size()) {
                var input = recipe.fluidInputs.get(i);
                slot.fluid(FluidVariant.of(input.fluid), input.amount, input.probability);
            } else {
                slot.variant(FluidVariant.blank());
            }
        }
    }

    private void addFluidOutputs(LayoutBuilder builder, MachineRecipe recipe) {
        var offset = getOffset();

        var positions = params.fluidOutputs;
        for (int i = 0; i < positions.size(); i++) {
            var x = offset.x + positions.getX(i);
            var y = offset.y + positions.getY(i);
            var slot = builder.outputSlot(x, y);

            if (i < recipe.fluidOutputs.size()) {
                var output = recipe.fluidOutputs.get(i);
                slot.fluid(FluidVariant.of(output.fluid), output.amount, output.probability);
            } else {
                slot.variant(FluidVariant.blank());
            }
        }
    }

    @Override
    public void buildWidgets(RecipeHolder<MachineRecipe> recipeHolder, WidgetList widgets) {
        var offset = getOffset();
        var recipe = recipeHolder.value();

        // Draw progress bar
        double recipeMillis = getSeconds(recipe) * 1000;
        widgets.drawable(matrices -> {
            ProgressBarClient.renderProgress(matrices, offset.x, offset.y, params.progressBarParams,
                    (float) (System.currentTimeMillis() / recipeMillis % 1.0));
        });

        // Draw filled energy bar
        widgets.drawable(guiGraphics -> {
            guiGraphics.pose().pushPose();

            guiGraphics.pose().translate(5, 5, 0);
            guiGraphics.pose().scale(0.5f, 0.5f, 0.5f);
            switch (params.steamMode) {
            case BOTH -> {
                guiGraphics.blit(MachineScreen.SLOT_ATLAS, -2, -2, 80, 18, 20, 20);
            }
            case STEAM_ONLY -> {
                guiGraphics.blit(new MIIdentifier("textures/item/steam_bucket.png"), 0, 0, 0, 0, 16, 16, 16, 16);
            }
            case ELECTRIC_ONLY -> {
                EnergyBarClient.Renderer.renderEnergy(guiGraphics, 0, 0, 1);
            }
            }

            guiGraphics.pose().popPose();
        });
        widgets.text(
                TextHelper.getEuTextTick(recipe.eu),
                15 + (params.steamMode.steam ? 2 : 0), 5, TextAlign.LEFT, false, true, null);
        widgets.text(
                MIText.BaseDurationSeconds.text(getSeconds(recipe)),
                width - 5, 5, TextAlign.RIGHT, false, true, null);

        // Draw steel hatch or upgrades
        boolean steelHatchRequired = params.steamMode.steam && params.isMultiblock && recipe.eu > MachineTier.BRONZE.getMaxEu();
        int upgradeEuRequired = recipe.eu - (params.isMultiblock ? MachineTier.MULTIBLOCK : MachineTier.LV).getMaxEu();
        // Ugly fusion reactor workaround
        if (upgradeEuRequired > 0 && id.getPath().equals("fusion_reactor")) {
            upgradeEuRequired = 0;
        }
        // Conditions
        boolean conditionsRequired = recipe.conditions.size() > 0;
        if (steelHatchRequired || upgradeEuRequired > 0 || conditionsRequired) {
            ItemLike displayedItem;
            if (steelHatchRequired) {
                displayedItem = BuiltInRegistries.ITEM.get(new MIIdentifier("steel_item_input_hatch"));
            } else if (conditionsRequired) {
                displayedItem = MIItem.WRENCH;
            } else {
                displayedItem = MIItem.BASIC_UPGRADE;
            }
            widgets.item(width / 2f - 3, 3.75, 10.8, 10.8, displayedItem);
        }
        // Tooltips
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(MIText.BaseEuTotal.text(TextHelper.getEuText((long) recipe.duration * recipe.eu)));
        if (params.steamMode.steam) {
            tooltips.add((params.steamMode.electric ? MIText.AcceptsSteamToo : MIText.AcceptsSteam).text().withStyle(ChatFormatting.GRAY));
            if (steelHatchRequired) {
                tooltips.add(MIText.RequiresSteelHatch0.text().setStyle(Style.EMPTY.withUnderlined(true)));
                tooltips.add(MIText.RequiresSteelHatch1.text().withStyle(ChatFormatting.GRAY));
            }
        }
        if (upgradeEuRequired > 0) {
            tooltips.add(new MITooltips.Line(MIText.RequiresUpgrades).arg(upgradeEuRequired, EU_PER_TICK_PARSER).build());
        }
        if (conditionsRequired) {
            for (var condition : recipe.conditions) {
                condition.appendDescription(tooltips);
            }
        }
        widgets.tooltip(2, 5, width - 10, 11, tooltips);
    }

    private double getSeconds(MachineRecipe recipe) {
        return recipe.duration / 20.0;
    }

    private DrawOffset getOffset() {
        // Compute offset relative to the machine coordinates
        int x = 1000, X = 0, y = 1000, Y = 0;
        for (SlotPositions positions : new SlotPositions[] { params.itemInputs, params.itemOutputs, params.fluidInputs, params.fluidOutputs }) {
            for (int i = 0; i < positions.size(); ++i) {
                x = Math.min(x, positions.getX(i));
                X = Math.max(X, positions.getX(i) + 16);
                y = Math.min(y, positions.getY(i));
                Y = Math.max(Y, positions.getY(i) + 16);
            }
        }
        int xoffset = (width - X + x) / 2 - x;
        int yoffset = 17 - y;

        return new DrawOffset(xoffset, yoffset);
    }

    private record DrawOffset(int x, int y) {
    }
}
