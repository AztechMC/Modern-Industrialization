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
package aztech.modern_industrialization;

import aztech.modern_industrialization.util.ResourceUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

public class MITags {
    public static final TagKey<Item> SCREWDRIVERS = item("screwdrivers");
    public static final TagKey<Item> WRENCHES = item("wrenches");

    public static final TagKey<Item> BARRELS = miItem("barrels");
    public static final TagKey<Item> TANKS = miItem("tanks");
    public static final TagKey<Item> FLUID_PIPES = miItem("fluid_pipes");
    public static final TagKey<Item> ITEM_PIPES = miItem("item_pipes");

    // Fabric should provide those:
    public static final TagKey<Item> AXES = item("axes");
    public static final TagKey<Item> PICKAXES = item("pickaxes");
    public static final TagKey<Item> SHOVELS = item("shovels");
    public static final TagKey<Item> SWORDS = item("swords");
    public static final TagKey<Item> SHULKER_BOX = item("shulker_box");

    public static void init() {
        dyeTags();
    }

    private static void dyeTags() {
        ResourceLocation terracottas = new ResourceLocation("c", "terracottas");
        ResourceLocation glass = new ResourceLocation("c", "glass");
        ResourceLocation glassPane = new ResourceLocation("c", "glass_pane");

        ResourceUtil.appendToItemTag(terracottas, new ResourceLocation("minecraft:terracotta"));
        ResourceUtil.appendToItemTag(glass, new ResourceLocation("minecraft:glass"));
        ResourceUtil.appendToItemTag(glassPane, new ResourceLocation("minecraft:glass_pane"));

        for (DyeColor color : DyeColor.values()) {
            ResourceLocation tagId = new ResourceLocation("c", color.getName() + "_dyes");
            ResourceUtil.appendToItemTag(tagId, new ResourceLocation("minecraft:" + color.getName() + "_dye"));
            ResourceUtil.appendToItemTag(terracottas, new ResourceLocation("minecraft:" + color.getName() + "_terracotta"));
            ResourceUtil.appendToItemTag(terracottas, new ResourceLocation("minecraft:" + color.getName() + "_glazed_terracotta"));

            ResourceUtil.appendToItemTag(glass, new ResourceLocation("minecraft:" + color.getName() + "_stained_glass"));
            ResourceUtil.appendToItemTag(glassPane, new ResourceLocation("minecraft:" + color.getName() + "_stained_glass_pane"));
        }
    }

    public static TagKey<Item> item(String path) {
        return TagKey.create(Registry.ITEM.key(), new ResourceLocation("c", path));
    }

    public static TagKey<Item> miItem(String path) {
        return TagKey.create(Registry.ITEM.key(), new MIIdentifier(path));
    }
}
