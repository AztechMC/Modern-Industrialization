package aztech.modern_industrialization.machinesv2.blockentities;

import aztech.modern_industrialization.api.energy.*;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machinesv2.MachineBlockEntity;
import aztech.modern_industrialization.machinesv2.components.EnergyComponent;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;
import aztech.modern_industrialization.machinesv2.components.sync.EnergyBar;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.helper.OrientationHelper;
import aztech.modern_industrialization.machinesv2.models.MachineCasingModel;
import aztech.modern_industrialization.machinesv2.models.MachineCasings;
import aztech.modern_industrialization.machinesv2.models.MachineModelClientData;
import aztech.modern_industrialization.util.RenderHelper;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;

public abstract class AbstractStorageMachineBlockEntity extends MachineBlockEntity implements Tickable {

    protected final OrientationComponent orientation;
    protected final EnergyComponent energy;

    protected final EnergyInsertable insertable;
    protected final EnergyExtractable extractable;

    protected final long eu_capacity;
    protected final CableTier from, to;

    public AbstractStorageMachineBlockEntity(BlockEntityType<?> type, CableTier from, CableTier to, String name, long eu_capacity) {
        super(type, new MachineGuiParameters.Builder(name, false).build());

        this.from = from;
        this.to = to;
        this.eu_capacity = eu_capacity;

        this.energy = new EnergyComponent(eu_capacity);
        insertable = energy.buildInsertable((CableTier tier)-> tier  == from);
        extractable = energy.buildExtractable((CableTier tier)-> tier  == to);
        EnergyBar.Parameters energyBarParams = new EnergyBar.Parameters(76, 39);
        registerClientComponent(new EnergyBar.Server(energyBarParams, energy::getEu, energy::getCapacity));


        this.orientation = new OrientationComponent(new OrientationComponent.Params(true, false, false));
    }


    @Override
    public MIInventory getInventory() {
        return MIInventory.EMPTY;
    }

    @Override
    protected ActionResult onUse(PlayerEntity player, Hand hand, Direction face) {
        return OrientationHelper.onUse(player, hand, face, orientation, this);
    }

    public static MachineCasingModel getCasingFromTier(CableTier from, CableTier to){
        return MachineCasings.casingFromCableTier(from.eu > to.eu ? from : to);
    }

    @Override
    protected MachineModelClientData getModelData() {
        MachineModelClientData data = new MachineModelClientData(getCasingFromTier(from, to));
        orientation.writeModelData(data);
        return data;
    }

    @Override
    public void onPlaced(LivingEntity placer, ItemStack itemStack) {
        orientation.onPlaced(placer, itemStack);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        orientation.readNbt(tag);
        RenderHelper.forceChunkRemesh((ClientWorld) world, pos);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        orientation.writeNbt(tag);
        return tag;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        energy.writeNbt(tag);
        orientation.writeNbt(tag);
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        energy.readNbt(tag);
        orientation.readNbt(tag);
    }

    @Override
    public void tick() {
        EnergyMoveable insertable = EnergyApi.MOVEABLE.get(world, pos.offset(orientation.outputDirection),
                orientation.outputDirection.getOpposite());
        if (insertable instanceof EnergyInsertable && ((EnergyInsertable) insertable).canInsert(to)) {
            energy.insertEnergy((EnergyInsertable) insertable);
        }
        markDirty();
    }

    public static void registerEnergyApi(BlockEntityType<?> bet) {
        EnergyApi.MOVEABLE.registerForBlockEntities((be, direction) -> {
                        AbstractStorageMachineBlockEntity abe = (AbstractStorageMachineBlockEntity) be;
                        if(abe.orientation.outputDirection == direction){
                            return abe.extractable;
                        }else{
                            return abe.insertable;
                        }
                        }
                , bet);
    }
}
