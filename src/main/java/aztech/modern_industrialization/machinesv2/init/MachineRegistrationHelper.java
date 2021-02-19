package aztech.modern_industrialization.machinesv2.init;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machinesv2.MachineBlock;
import net.devtech.arrp.json.tags.JTag;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class MachineRegistrationHelper {
    /**
     * Register a machine's block, block entity type and wrenchable tag.
     * @param id Machine block id, for example "lv_macerator"
     * @param factory The block entity constructor, with a BET parameter.
     * @param extraRegistrator A BET consumer used for API registration.
     */
    public static void registerMachine(String id, Function<BlockEntityType<?>, BlockEntity> factory, Consumer<BlockEntityType<?>> extraRegistrator) {
        BlockEntityType<?>[] bet = new BlockEntityType[1];
        Supplier<BlockEntity> ctor = () -> factory.apply(bet[0]);
        Block block = new MachineBlock(id, ctor);
        bet[0] = Registry.register(Registry.BLOCK_ENTITY_TYPE, new MIIdentifier(id), BlockEntityType.Builder.create(ctor, block).build(null));
        ModernIndustrialization.RESOURCE_PACK.addTag(new Identifier("fabric:blocks/wrenchables"), JTag.tag().add(new MIIdentifier(id)));
        extraRegistrator.accept(bet[0]);
    }
}
