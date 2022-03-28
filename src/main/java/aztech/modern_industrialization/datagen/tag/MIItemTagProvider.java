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
package aztech.modern_industrialization.datagen.tag;

import aztech.modern_industrialization.MITags;
import aztech.modern_industrialization.machines.blockentities.ReplicatorMachineBlockEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

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
