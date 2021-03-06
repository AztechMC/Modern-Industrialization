package aztech.modern_industrialization.machinesv2.blockentities.multiblocks;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machinesv2.MachineBlockEntity;
import aztech.modern_industrialization.machinesv2.components.CrafterComponent;
import aztech.modern_industrialization.machinesv2.components.IsActiveComponent;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.helper.OrientationHelper;
import aztech.modern_industrialization.machinesv2.helper.SteamHelper;
import aztech.modern_industrialization.machinesv2.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machinesv2.models.MachineModelClientData;
import aztech.modern_industrialization.machinesv2.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machinesv2.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machinesv2.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.recipe.MIRecipes;
import aztech.modern_industrialization.util.ChunkUnloadBlockEntity;
import aztech.modern_industrialization.util.Simulation;
import com.google.common.base.Preconditions;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class SteamCraftingMultiblockBlockEntity extends MachineBlockEntity implements Tickable, ChunkUnloadBlockEntity {
    public SteamCraftingMultiblockBlockEntity(BlockEntityType<?> type, String name, ShapeTemplate shapeTemplate) {
        super(type, new MachineGuiParameters.Builder(name, false).build());

        this.orientation = new OrientationComponent(new OrientationComponent.Params(false, false, false));
        this.shapeTemplate = shapeTemplate;
        this.aggregate = new AggregateInventory();
        this.crafterBehavior = new Behavior(MIMachineRecipeTypes.COKE_OVEN);
        this.crafter = new CrafterComponent(aggregate, crafterBehavior);
        this.isActive = new IsActiveComponent();
        registerComponents(orientation, crafter, isActive);
    }

    public final OrientationComponent orientation;
    public final ShapeTemplate shapeTemplate;
    private final AggregateInventory aggregate;
    private final Behavior crafterBehavior;
    private final CrafterComponent crafter;
    private final IsActiveComponent isActive;
    @Nullable
    private ShapeMatcher shapeMatcher = null;
    private boolean allowNormalOperation = false;

    @Override
    public MIInventory getInventory() {
        return MIInventory.EMPTY;
    }

    @Override
    protected ActionResult onUse(PlayerEntity player, Hand hand, Direction face) {
        ActionResult result = OrientationHelper.onUse(player, hand, face, orientation, this);
        if (result.isAccepted() && world.isClient) {
            unlink();
        }
        return result;
    }

    @Override
    protected MachineModelClientData getModelData() {
        return new MachineModelClientData(null, orientation.facingDirection).active(isActive.isActive);
    }

    @Override
    public void onPlaced(LivingEntity placer, ItemStack itemStack) {
        orientation.onPlaced(placer, itemStack);
    }

    @Override
    public void tick() {
        if (!world.isClient) {
            link();

            boolean newActive = false;

            if (allowNormalOperation) {
                if (crafter.tickRecipe()) {
                    newActive = true;
                }
            }

            isActive.updateActive(newActive, this);
        }
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        if (!world.isClient) {
            unlink();
        }
    }

    @Override
    public void onChunkUnload() {
        unlink();
    }

    private void link() {
        if (shapeMatcher == null) {
            shapeMatcher = new ShapeMatcher(world, pos, orientation.facingDirection, shapeTemplate);
            shapeMatcher.registerListeners(world);
        }
        if (shapeMatcher.needsRematch()) {
            allowNormalOperation = false;
            shapeMatcher.rematch(world);

            if (shapeMatcher.isMatchSuccessful()) {
                aggregate.rebuild();

                // If there was an active recipe, we have to make sure the output fits, and lock the hatches.
                if (crafter.tryContinueRecipe()) {
                    allowNormalOperation = true;
                }
            }
        }
    }

    private void unlink() {
        if (shapeMatcher != null) {
            shapeMatcher.unlinkHatches();
            shapeMatcher.unregisterListeners(world);
            shapeMatcher = null;
        }
    }

    private class AggregateInventory implements CrafterComponent.Inventory {
        private final List<ConfigurableItemStack> itemInputs = new ArrayList<>();
        private final List<ConfigurableItemStack> itemOutputs = new ArrayList<>();
        private final List<ConfigurableFluidStack> fluidInputs = new ArrayList<>();
        private final List<ConfigurableFluidStack> fluidOutputs = new ArrayList<>();
        boolean steelTier;

        public void rebuild() {
            rebuildList(itemInputs, HatchBlockEntity::appendItemInputs);
            rebuildList(itemOutputs, HatchBlockEntity::appendItemOutputs);
            rebuildList(fluidInputs, HatchBlockEntity::appendFluidInputs);
            rebuildList(fluidOutputs, HatchBlockEntity::appendFluidOutputs);
            steelTier = false;
            for (HatchBlockEntity hatch : shapeMatcher.getMatchedHatches()) {
                if (hatch.upgradesToSteel()) {
                    steelTier = true;
                }
            }
        }

        private <Stack> void rebuildList(List<Stack> stacks, BiConsumer<HatchBlockEntity, List<Stack>> appender) {
            stacks.clear();
            for (HatchBlockEntity hatch : shapeMatcher.getMatchedHatches()) {
                appender.accept(hatch, stacks);
            }
        }

        @Override
        public List<ConfigurableItemStack> getItemInputs() {
            return itemInputs;
        }

        @Override
        public List<ConfigurableItemStack> getItemOutputs() {
            return itemOutputs;
        }

        @Override
        public List<ConfigurableFluidStack> getFluidInputs() {
            return fluidInputs;
        }

        @Override
        public List<ConfigurableFluidStack> getFluidOutputs() {
            return fluidOutputs;
        }
    }

    private class Behavior implements CrafterComponent.Behavior {
        private final MachineRecipeType recipeType;

        Behavior(MachineRecipeType recipeType) {
            this.recipeType = recipeType;
        }

        @Override
        public long consumeEu(long max, Simulation simulation) {
            return SteamHelper.consumeSteamEu(aggregate.getFluidInputs(), max, simulation);
        }

        @Override
        public MachineRecipeType recipeType() {
            return recipeType;
        }

        @Override
        public long getBaseRecipeEu() {
            return aggregate.steelTier ? 4 : 2;
        }

        @Override
        public long getMaxRecipeEu() {
            return getBaseRecipeEu();
        }

        @Override
        public World getWorld() {
            return world;
        }
    }
}
