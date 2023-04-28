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
package aztech.modern_industrialization.machines.blockentities.multiblocks;

import static aztech.modern_industrialization.machines.multiblocks.HatchType.*;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.compat.kubejs.KubeJSProxy;
import aztech.modern_industrialization.compat.megane.holder.EnergyListComponentHolder;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.*;
import aztech.modern_industrialization.machines.guicomponents.ShapeSelection;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.init.MachineTier;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.*;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.util.Simulation;
import com.google.common.base.Preconditions;
import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

// TODO: should the common part with ElectricCraftingMultiblockBlockEntity be refactored?
public class ElectricBlastFurnaceBlockEntity extends AbstractCraftingMultiblockBlockEntity implements EnergyListComponentHolder {

    public record Tier(ResourceLocation coilBlockId, long maxBaseEu, String englishName) {
        public String getTranslationKey() {
            return "ebf_tier.modern_industrialization." + coilBlockId.getPath();
        }

        public Component getDisplayName() {
            return Component.translatable(getTranslationKey());
        }
    }

    public static final List<Tier> tiers;
    public static final Map<ResourceLocation, Tier> tiersByCoil;
    private static final ShapeTemplate[] shapeTemplates;

    static {
        // Register tiers
        List<Tier> registrationTiers = new ArrayList<>();

        registrationTiers.add(new Tier(new MIIdentifier("cupronickel_coil"), 32, "Cupronickel"));
        registrationTiers.add(new Tier(new MIIdentifier("kanthal_coil"), 128, "Kanthal"));
        KubeJSProxy.instance.fireAddEbfTiersEvent(tier -> {
            Preconditions.checkArgument(tier.maxBaseEu > 4, "EBF tier EU/t must be greater than 4.");
            for (var t : registrationTiers) {
                if (t.coilBlockId.equals(tier.coilBlockId)) {
                    throw new IllegalArgumentException("EBF tier with coil " + tier.coilBlockId + " is already registered.");
                }
                if (t.coilBlockId.getPath().equals(tier.coilBlockId.getPath())) {
                    throw new IllegalArgumentException(
                            "EBF tier with coil " + tier.coilBlockId + " has the same path as an already registered tier.");
                }
                if (t.maxBaseEu == tier.maxBaseEu) {
                    throw new IllegalArgumentException("EBF tier with max " + tier.maxBaseEu + " EU/t is already registered.");
                }
            }
            registrationTiers.add(tier);
        });
        registrationTiers.sort(Comparator.comparingLong(Tier::maxBaseEu));

        tiers = Collections.unmodifiableList(registrationTiers);
        tiersByCoil = tiers.stream().collect(Collectors.toMap(Tier::coilBlockId, t -> t));

        // Build shapes
        shapeTemplates = new ShapeTemplate[tiers.size()];

        for (int i = 0; i < tiers.size(); ++i) {
            var tier = tiers.get(i);
            SimpleMember invarCasings = SimpleMember.forBlock(MIBlock.BLOCKS.get(new MIIdentifier("heatproof_machine_casing")).asBlock());
            SimpleMember coilsBlocks = SimpleMember.forBlockId(tier.coilBlockId());
            HatchFlags ebfHatches = new HatchFlags.Builder().with(ITEM_INPUT, ITEM_OUTPUT, FLUID_INPUT, FLUID_OUTPUT, ENERGY_INPUT).build();
            ShapeTemplate ebfShape = new ShapeTemplate.Builder(MachineCasings.HEATPROOF)
                    .add3by3(0, invarCasings, false, ebfHatches)
                    .add3by3(1, coilsBlocks, true, null)
                    .add3by3(2, coilsBlocks, true, null)
                    .add3by3(3, invarCasings, false, ebfHatches)
                    .build();
            shapeTemplates[i] = ebfShape;
        }
    }

    public ElectricBlastFurnaceBlockEntity(BEP bep) {
        super(bep, "electric_blast_furnace", new OrientationComponent.Params(false, false, false), shapeTemplates);
        this.upgrades = new UpgradeComponent();
        this.registerComponents(upgrades);
        registerGuiComponent(new SlotPanel.Server(this).withUpgrades(upgrades));

        var tierComponents = tiers.stream().map(Tier::getDisplayName).toList();

        registerGuiComponent(new ShapeSelection.Server(new ShapeSelection.Behavior() {
            @Override
            public void handleClick(int clickedLine, int delta) {
                activeShape.incrementShape(ElectricBlastFurnaceBlockEntity.this, delta);
            }

            @Override
            public int getCurrentIndex(int line) {
                return activeShape.getActiveShapeIndex();
            }
        }, new ShapeSelection.LineInfo(tiers.size(), tierComponents, true)));
    }

    @Override
    protected CrafterComponent.Behavior getBehavior() {
        return new Behavior();
    }

    private final List<EnergyComponent> energyInputs = new ArrayList<>();
    private final UpgradeComponent upgrades;

    @Override
    public List<EnergyComponent> getEnergyComponents() {
        return energyInputs;
    }

    @Override
    protected void onSuccessfulMatch(ShapeMatcher shapeMatcher) {
        energyInputs.clear();
        for (HatchBlockEntity hatch : shapeMatcher.getMatchedHatches()) {
            hatch.appendEnergyInputs(energyInputs);
        }
    }

    protected InteractionResult onUse(Player player, InteractionHand hand, Direction face) {
        InteractionResult result = super.onUse(player, hand, face);
        if (!result.consumesAction()) {
            result = upgrades.onUse(this, player, hand);
        }
        if (!result.consumesAction()) {
            result = LubricantHelper.onUse(this.crafter, player, hand);
        }
        return result;
    }

    @Override
    public List<ItemStack> dropExtra() {
        List<ItemStack> drops = super.dropExtra();
        drops.add(upgrades.getDrop());
        return drops;
    }

    private class Behavior implements CrafterComponent.Behavior {
        @Override
        public long consumeEu(long max, Simulation simulation) {
            long total = 0;

            for (EnergyComponent energyComponent : energyInputs) {
                total += energyComponent.consumeEu(max - total, simulation);
            }

            return total;
        }

        @Override
        public MachineRecipeType recipeType() {
            return MIMachineRecipeTypes.BLAST_FURNACE;
        }

        public boolean banRecipe(MachineRecipe recipe) {
            int index = activeShape.getActiveShapeIndex();
            return (recipe.eu > getMaxRecipeEu()) || (recipe.eu > tiers.get(index).maxBaseEu);
        }

        @Override
        public long getBaseRecipeEu() {
            return MachineTier.MULTIBLOCK.getBaseEu();
        }

        @Override
        public long getMaxRecipeEu() {
            return MachineTier.MULTIBLOCK.getMaxEu() + upgrades.getAddMaxEUPerTick();
        }

        @Override
        public Level getCrafterWorld() {
            return level;
        }

        @Override
        @Nullable
        public UUID getOwnerUuid() {
            return placedBy.placerId;
        }
    }

    public static void registerReiShapes() {
        for (ShapeTemplate shapeTemplate : shapeTemplates) {
            ReiMachineRecipes.registerMultiblockShape("electric_blast_furnace", shapeTemplate);
        }
    }
}
