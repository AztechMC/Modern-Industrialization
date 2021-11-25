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

import aztech.modern_industrialization.machines.blockentities.ReplicatorMachineBlockEntity;
import aztech.modern_industrialization.util.ResourceUtil;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class MITags {
    public static void init() {
        dyeTags();
        ReplicatorMachineBlockEntity.initTag();
    }

    private static void dyeTags() {
        Identifier terracottas = new Identifier("c", "terracottas");
        Identifier glass = new Identifier("c", "glass");
        Identifier glassPane = new Identifier("c", "glass_pane");
        Identifier shulkerBox = new Identifier("c", "shulker_box");

        ResourceUtil.appendToItemTag(terracottas, new Identifier("minecraft:terracotta"));
        ResourceUtil.appendToItemTag(glass, new Identifier("minecraft:glass"));
        ResourceUtil.appendToItemTag(glassPane, new Identifier("minecraft:glass_pane"));
        ResourceUtil.appendToItemTag(shulkerBox, new Identifier("minecraft:shulker_box"));

        for (DyeColor color : DyeColor.values()) {
            Identifier tagId = new Identifier("c", color.getName() + "_dyes");
            TagRegistry.item(tagId);
            ResourceUtil.appendToItemTag(tagId, new Identifier("minecraft:" + color.getName() + "_dye"));
            ResourceUtil.appendToItemTag(terracottas, new Identifier("minecraft:" + color.getName() + "_terracotta"));
            ResourceUtil.appendToItemTag(terracottas, new Identifier("minecraft:" + color.getName() + "_glazed_terracotta"));

            ResourceUtil.appendToItemTag(glass, new Identifier("minecraft:" + color.getName() + "_stained_glass"));
            ResourceUtil.appendToItemTag(glassPane, new Identifier("minecraft:" + color.getName() + "_stained_glass_pane"));
            ResourceUtil.appendToItemTag(shulkerBox, new Identifier("minecraft:" + color.getName() + "_shulker_box"));
        }
    }
}
