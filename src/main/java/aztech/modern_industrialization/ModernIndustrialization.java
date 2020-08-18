package aztech.modern_industrialization;

import aztech.modern_industrialization.block.MachineBlock;
import aztech.modern_industrialization.blockentity.steam.SteamBoilerBlockEntity;
import aztech.modern_industrialization.blockentity.steam.SteamFurnaceBlockEntity;
import aztech.modern_industrialization.fluid.CraftingFluid;
import aztech.modern_industrialization.fluid.FluidStackItem;
import aztech.modern_industrialization.gui.MachineScreenHandler;
import aztech.modern_industrialization.gui.SteamBoilerScreenHandler;
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
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	public static final Block BLOCK_STEAM_BOILER = new MachineBlock(SteamBoilerBlockEntity::new);
	public static final Item ITEM_STEAM_BOILER = new BlockItem(BLOCK_STEAM_BOILER, new Item.Settings().group(ITEM_GROUP));
	public static final Block BLOCK_STEAM_FURNACE =  new MachineBlock(SteamFurnaceBlockEntity::new);
	public static final Item ITEM_STEAM_FURNACE =  new BlockItem(BLOCK_STEAM_FURNACE, new Item.Settings().group(ITEM_GROUP));

	// BlockEntity
	public static BlockEntityType<SteamBoilerBlockEntity> BLOCK_ENTITY_STEAM_BOILER;
	public static BlockEntityType<SteamFurnaceBlockEntity> BLOCK_ENTITY_STEAM_FURNACE;

	// ScreenHandlerType
	public static final ScreenHandlerType<SteamBoilerScreenHandler> SCREEN_HANDLER_TYPE_STEAM_BOILER = ScreenHandlerRegistry.registerSimple(new Identifier(MOD_ID, "steam_boiler"), SteamBoilerScreenHandler::new);
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

		MIPipes.INSTANCE.onInitialize();

		RRPCallback.EVENT.register(a -> a.add(RESOURCE_PACK));

		LOGGER.info("Modern Industrialization setup done!");
	}

	private void setupItems() {
		registerItem(ITEM_WRENCH, "wrench");
	}

	private void setupBlocks() {
		registerBlock(BLOCK_STEAM_BOILER, ITEM_STEAM_BOILER,"steam_boiler");
		registerBlock(BLOCK_STEAM_FURNACE, ITEM_STEAM_FURNACE,"steam_furnace");
	}

	private void setupBlockEntities() {
		BLOCK_ENTITY_STEAM_BOILER = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "steam_boiler"), BlockEntityType.Builder.create(SteamBoilerBlockEntity::new, BLOCK_STEAM_BOILER).build(null));
		BLOCK_ENTITY_STEAM_FURNACE = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "steam_furnace"), BlockEntityType.Builder.create(SteamFurnaceBlockEntity::new, BLOCK_STEAM_FURNACE).build(null));
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

	private void registerItem(Item item, String id) {
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, id), item);
		RESOURCE_PACK.addModel(JModel.model().parent("minecraft:item/generated").textures(new JTextures().layer0(MOD_ID + ":items/" + id)), new MIIdentifier("item/" + id));
	}

	private void registerFluid(Fluid fluid, String id) {
		Registry.register(Registry.FLUID, new MIIdentifier(id), fluid);
		Registry.register(Registry.ITEM, new MIIdentifier("bucket_"+id), fluid.getBucketItem());
		RESOURCE_PACK.addModel(JModel.model().parent("minecraft:item/generated").textures(new JTextures().layer0(MOD_ID + ":items/bucket/" + id)), new MIIdentifier("item/bucket_" + id));
	}
}
