package aztech.modern_industrialization.blocks.tank;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.fluid.FluidUnit;
import aztech.modern_industrialization.model.block.ModelProvider;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;

import static aztech.modern_industrialization.ModernIndustrialization.ITEM_GROUP;

public enum MITanks {
    BRONZE("bronze", 4),
    STEEL("steel", 16)
    ;

    public static BlockEntityType<TankBlockEntity> BLOCK_ENTITY_TYPE;

    public final String type;
    public final Block block;
    public final Item item;

    MITanks(String type, int bucketCapacity) {
        this.type = type;
        this.block = new TankBlock(FabricBlockSettings.of(Material.METAL).hardness(4.0f));
        this.item = new TankItem(block, new Item.Settings().group(ITEM_GROUP), FluidUnit.DROPS_PER_BUCKET * bucketCapacity);
    }

    public static void setup() {
        for(MITanks tank : values()) {
            ModernIndustrialization.registerBlock(tank.block, tank.item, "tank_" + tank.type, 0);
        }
        BLOCK_ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, new MIIdentifier("tank"), BlockEntityType.Builder.create(TankBlockEntity::new, getBlocks()).build(null));
    }

    public static void setupClient() {
        for(MITanks tank : values()) {
            UnbakedModel tankModel = new TankModel(tank.type);
            ModelProvider.modelMap.put(new MIIdentifier("block/tank_" + tank.type), tankModel);
            ModelProvider.modelMap.put(new MIIdentifier("item/tank_" + tank.type), tankModel);
        }
    }

    private static Block[] getBlocks() {
        return Arrays.stream(values()).map(x -> x.block).toArray(Block[]::new);
    }
}
