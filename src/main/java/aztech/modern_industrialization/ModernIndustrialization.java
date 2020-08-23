package aztech.modern_industrialization;

import aztech.modern_industrialization.fluid.CraftingFluid;
import aztech.modern_industrialization.fluid.FluidStackItem;
import aztech.modern_industrialization.machines.MIMachines;
import aztech.modern_industrialization.machines.impl.MachineBlock;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.MachineScreenHandler;
import aztech.modern_industrialization.material.MIMaterial;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.tools.WrenchItem;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.blockstate.JBlockModel;
import net.devtech.arrp.json.blockstate.JState;
import net.devtech.arrp.json.blockstate.JVariant;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

//import aztech.modern_industrialization.machines.impl.SteamBoilerScreenHandler;

public class ModernIndustrialization implements ModInitializer {
	public static final String MOD_ID = "modern_industrialization";
	public static final Logger LOGGER = LogManager.getLogger("Modern Industrialization");
	public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create("modern_industrialization:general");

	public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
			new Identifier(MOD_ID, "general"),
			() -> new ItemStack(Items.REDSTONE)
	);
	public static final Item ITEM_FLUID_SLOT = new FluidStackItem(new Item.Settings().maxCount(1)); // evil hack

	// Item
	public static final Item ITEM_WRENCH = new WrenchItem(new Item.Settings());

	// Block
	//public static final Block BLOCK_STEAM_BOILER = new MachineBlock(SteamBoilerBlockEntity::new);
	//public static final Item ITEM_STEAM_BOILER = new BlockItem(BLOCK_STEAM_BOILER, new Item.Settings().group(ITEM_GROUP));

	// BlockEntity
	//public static BlockEntityType<SteamBoilerBlockEntity> BLOCK_ENTITY_STEAM_BOILER;

	// ScreenHandlerType
	//public static final ScreenHandlerType<SteamBoilerScreenHandler> SCREEN_HANDLER_TYPE_STEAM_BOILER = ScreenHandlerRegistry.registerSimple(new Identifier(MOD_ID, "steam_boiler"), SteamBoilerScreenHandler::new);
	public static final ScreenHandlerType<MachineScreenHandler> SCREEN_HANDLER_TYPE_MACHINE = ScreenHandlerRegistry.registerExtended(new Identifier(MOD_ID, "machine"), MachineScreenHandler::new);
	// Fluid
	public static final Fluid FLUID_STEAM = new CraftingFluid();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		setupItems();
		setupBlocks();
		setupBlockEntities();
		setupFluids();
		setupMaterial();
		MIMachines.setupRecipes(); // will also load the static fields.
		setupMachines();

		MIPipes.INSTANCE.onInitialize();

		RRPCallback.EVENT.register(a -> a.add(RESOURCE_PACK));

		LOGGER.info("Modern Industrialization setup done!");
	}

	private void setupMaterial() {
		for(MIMaterial material : MIMaterial.getAllMaterials()){
			registerMaterial(material);
		}
	}

	private void setupItems() {
		registerItem(ITEM_WRENCH, "wrench");
	}

	private void setupBlocks() {
		//registerBlock(BLOCK_STEAM_BOILER, ITEM_STEAM_BOILER,"steam_boiler");

	}

	private void setupBlockEntities() {
		//BLOCK_ENTITY_STEAM_BOILER = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "steam_boiler"), BlockEntityType.Builder.create(SteamBoilerBlockEntity::new, BLOCK_STEAM_BOILER).build(null));
	}

	private void setupMachines() {
		for(MachineFactory factory : MachineFactory.getFactories()) {
			factory.block = new MachineBlock(factory.blockEntityConstructor);
			factory.item = new BlockItem(factory.block, new Item.Settings().group(ITEM_GROUP));
			registerBlock(factory.block, factory.item,factory.getID());
			factory.blockEntityType = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, factory.getID()), BlockEntityType.Builder.create(factory.blockEntityConstructor, factory.block).build(null));
		}
	}

	private void setupFluids() {
		registerFluid(FLUID_STEAM, "steam");

		Registry.register(Registry.ITEM, new MIIdentifier("fluid_slot"), ITEM_FLUID_SLOT);
	}

	private void registerBlock(Block block, Item item, String id) {
		Identifier identifier = new MIIdentifier(id);
		Registry.register(Registry.BLOCK, identifier, block);
		Registry.register(Registry.ITEM, identifier, item);

		// TODO: client side?
		RESOURCE_PACK.addBlockState(JState.state().add(new JVariant().put("", new JBlockModel(MOD_ID + ":block/" + id))), identifier);
	}

	private void registerMaterial(MIMaterial material){
		String id = material.getId();

		LinkedList<String> item_types = new LinkedList<>();
		Collections.addAll(item_types, material.customItem);

		if(!material.isVanilla()){
			for(String block_type : (material.hasOre() ? new String[] {"block", "ore"} : new String[] {"block"}) ){
				Block block = null;
				if(block_type.equals("block")) {
					block =  new Block(FabricBlockSettings.of(Material.METAL).hardness(material.getHardness())
							.resistance(material.getBlastResistance()));
				}else if(block_type.equals("ore")){
					block =  new Block(FabricBlockSettings.of(Material.STONE).hardness(material.getOreHardness())
							.resistance(material.getOreBlastResistance()));

					// TODO : Add ore generation
				}

				Item item = new BlockItem(block, new Item.Settings().group(ITEM_GROUP));
				Identifier identifier = new MIIdentifier(id + "_"+block_type);
				material.saveBlock(block_type, block);
				Registry.register(Registry.BLOCK, identifier, block);
				Registry.register(Registry.ITEM, identifier, item);
				RESOURCE_PACK.addBlockState(JState.state().add(
						new JVariant().put("", new JBlockModel(MOD_ID + ":block/materials/" + id+"/"+block_type))), identifier);
			}
			item_types.add("ingot");
			item_types.add("nugget");

		}

		for(String item_type : item_types){
			Item item = new Item(new Item.Settings().group(ITEM_GROUP));
			material.saveItem(item_type, item);
			String custom_id = id+"_"+item_type;
			Registry.register(Registry.ITEM, new MIIdentifier(custom_id), item);
			RESOURCE_PACK.addModel(JModel.model().parent("minecraft:item/generated")
					.textures(new JTextures().layer0(MOD_ID + ":items/materials/" + id+"/"+item_type)), new MIIdentifier("item/" + custom_id));

		}


	}

	private void registerItem(Item item, String id) {
		Registry.register(Registry.ITEM, new MIIdentifier(id), item);
		RESOURCE_PACK.addModel(JModel.model().parent("minecraft:item/generated").textures(new JTextures().layer0(MOD_ID + ":items/" + id)), new MIIdentifier("item/" + id));
	}

	private void registerFluid(Fluid fluid, String id) {
		Registry.register(Registry.FLUID, new MIIdentifier(id), fluid);
		Registry.register(Registry.ITEM, new MIIdentifier("bucket_"+id), fluid.getBucketItem());
		RESOURCE_PACK.addModel(JModel.model().parent("minecraft:item/generated").textures(new JTextures().layer0(MOD_ID + ":items/bucket/" + id)), new MIIdentifier("item/bucket_" + id));
	}
}
