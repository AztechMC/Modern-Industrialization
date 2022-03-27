package aztech.modern_industrialization.datagen.tag;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MITags;
import aztech.modern_industrialization.blocks.storage.tank.TankBlock;
import aztech.modern_industrialization.machines.blockentities.ReplicatorMachineBlockEntity;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MIItemTagProvider extends FabricTagProvider.ItemTagProvider {
	private static final Map<String, List<Item>> tagToItemMap = new HashMap<>();

	public static void generateTag(String tag, Item item) {
		if (tag.startsWith("#")) {
			throw new IllegalArgumentException("Tag must not start with #: " + tag);
		}
		tagToItemMap.computeIfAbsent(tag, t -> new ArrayList<>()).add(item);
	}

	public static void generateTag(String tag, String item) {
		generateTag(tag, Registry.ITEM.get(new ResourceLocation(item)));
	}

	public static void generateTag(TagKey<Item> tag, Item item) {
		generateTag(tag.location().toString(), item);
	}

	public MIItemTagProvider(FabricDataGenerator dataGenerator) {
		super(dataGenerator, null);
	}

	@Override
	protected void generateTags() {
		for (var entry : tagToItemMap.entrySet()) {
			var tagId = new ResourceLocation(entry.getKey());
			for (var item : entry.getValue()) {
				tag(key(tagId)).add(item);
			}
		}

		var shulkerBoxes = tag(MITags.SHULKER_BOX).add(Items.SHULKER_BOX);
		for (DyeColor color : DyeColor.values()) {
			shulkerBoxes.add(ResourceKey.create(Registry.ITEM.key(), new ResourceLocation("minecraft:" + color.getName() + "_shulker_box")));
		}

		tag(ReplicatorMachineBlockEntity.BLACKLISTED)
				.add(Items.BUNDLE)
				.addTag(MITags.SHULKER_BOX)
				.addTag(MITags.TANKS)
				.addTag(MITags.BARRELS);
	}

	private static TagKey<Item> key(ResourceLocation id) {
		return TagKey.create(Registry.ITEM.key(), id);
	}
}
