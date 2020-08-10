package aztech.modern_industrialization;

import aztech.modern_industrialization.block.MachineBlock;
import aztech.modern_industrialization.blockentity.SteamBoilerBlockEntity;
import aztech.modern_industrialization.fluid.CraftingFluid;
import aztech.modern_industrialization.fluid.FluidStackItem;
import aztech.modern_industrialization.gui.SteamBoilerScreenHandler;
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

	public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
			new Identifier(MOD_ID, "general"),
			() -> new ItemStack(Items.REDSTONE)
	);
	public static final Item ITEM_FLUID_SLOT = new FluidStackItem(new Item.Settings().maxCount(1)); // evil hack

	// Block
	public static final Block BLOCK_STEAM_BOILER = new MachineBlock(SteamBoilerBlockEntity::new);

	// BlockEntity
	public static BlockEntityType<SteamBoilerBlockEntity> BLOCK_ENTITY_STEAM_BOILER;

	// ScreenHandlerType
	public static final ScreenHandlerType<SteamBoilerScreenHandler> SCREEN_HANDLER_TYPE_STEAM_BOILER = ScreenHandlerRegistry.registerSimple(new Identifier(MOD_ID, "steam_boiler"), SteamBoilerScreenHandler::new);

	// Fluid
	public static final Fluid FLUID_STEAM = new CraftingFluid();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		setupBlocks();
		setupBlockEntities();
		setupFluids();

		LOGGER.info("Modern Industrialization setup done!");
	}

	private void setupBlocks() {
		registerBlock(BLOCK_STEAM_BOILER, "steam_boiler");
	}

	private void setupBlockEntities() {
		BLOCK_ENTITY_STEAM_BOILER = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "steam_boiler"), BlockEntityType.Builder.create(SteamBoilerBlockEntity::new, BLOCK_STEAM_BOILER).build(null));
	}

	private void setupFluids() {
		registerFluid(FLUID_STEAM, "steam");

		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "fluid_slot"), ITEM_FLUID_SLOT);
	}

	private void registerBlock(Block block, String id) {
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, id), block);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, id), new BlockItem(block, new Item.Settings().group(ITEM_GROUP)));
	}

	private void registerFluid(Fluid fluid, String id) {
		Registry.register(Registry.FLUID, new Identifier(MOD_ID, id), fluid);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, id + "_bucket"), fluid.getBucketItem());
	}
}
