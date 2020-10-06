package aztech.modern_industrialization.blocks.tank;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.blocks.creativetank.CreativeTankBlock;
import aztech.modern_industrialization.blocks.creativetank.CreativeTankBlockEntity;
import aztech.modern_industrialization.blocks.creativetank.CreativeTankItem;
import aztech.modern_industrialization.blocks.creativetank.CreativeTankRenderer;
import aztech.modern_industrialization.model.block.ModelProvider;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
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
    STEEL("steel", 8),
    ALUMINUM("aluminum", 16),
    STAINLESS_STEEL("stainless_steel", 32)
    ;

    public static BlockEntityType<TankBlockEntity> BLOCK_ENTITY_TYPE;
    public static BlockEntityType<CreativeTankBlockEntity> CREATIVE_BLOCK_ENTITY_TYPE;
    public static CreativeTankBlock CREATIVE_TANK_BLOCK = new CreativeTankBlock(FabricBlockSettings.of(Material.METAL).hardness(4.0f));
    public static CreativeTankItem CREATIVE_TANK_ITEM = new CreativeTankItem(CREATIVE_TANK_BLOCK, new Item.Settings().group(ITEM_GROUP));

    public final String type;
    public final Block block;
    public final Item item;
    public final int bucketCapacity;

    MITanks(String type, int bucketCapacity) {
        this.type = type;
        this.block = new TankBlock(FabricBlockSettings.of(Material.METAL).hardness(4.0f));
        this.item = new TankItem(block, new Item.Settings().group(ITEM_GROUP), 1000 * bucketCapacity);
        this.bucketCapacity = bucketCapacity;
    }

    public static void setup() {
        ModernIndustrialization.registerBlock(CREATIVE_TANK_BLOCK, CREATIVE_TANK_ITEM, "creative_tank");
        for(MITanks tank : values()) {
            ModernIndustrialization.registerBlock(tank.block, tank.item, "tank_" + tank.type, 0);
        }
        BLOCK_ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, new MIIdentifier("tank"), BlockEntityType.Builder.create(TankBlockEntity::new, getBlocks()).build(null));
        CREATIVE_BLOCK_ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, new MIIdentifier("creative_tank"), BlockEntityType.Builder.create(CreativeTankBlockEntity::new, CREATIVE_TANK_BLOCK).build(null));
    }

    public static void setupClient() {
        for(MITanks tank : values()) {
            UnbakedModel tankModel = new TankModel(tank.type);
            ModelProvider.modelMap.put(new MIIdentifier("block/tank_" + tank.type), tankModel);
            ModelProvider.modelMap.put(new MIIdentifier("item/tank_" + tank.type), tankModel);
        }
        UnbakedModel creativeTankModel = new TankModel("creative");
        ModelProvider.modelMap.put(new MIIdentifier("block/creative_tank"), creativeTankModel);
        ModelProvider.modelMap.put(new MIIdentifier("item/creative_tank"), creativeTankModel);

        BlockEntityRendererRegistry.INSTANCE.register(BLOCK_ENTITY_TYPE, TankRenderer::new);
        BlockEntityRendererRegistry.INSTANCE.register(CREATIVE_BLOCK_ENTITY_TYPE, CreativeTankRenderer::new);
    }

    private static Block[] getBlocks() {
        return Arrays.stream(values()).map(x -> x.block).toArray(Block[]::new);
    }
}
