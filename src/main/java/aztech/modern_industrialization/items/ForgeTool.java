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

package aztech.modern_industrialization.items;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.datagen.tag.TagsToGenerate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ForgeTool extends Item {
    public static final TagKey<Item> TAG = TagKey.create(BuiltInRegistries.ITEM.key(), MI.id("forge_hammer_tools"));

    public ForgeTool(Properties p, int baseMaterialDurability, int enchantmentValue) {
        super(applyForgeHammerProperties(p, baseMaterialDurability, enchantmentValue));
        TagsToGenerate.generateTag(TAG, this, "Forge Hammer Tools");
    }

    private static Properties applyForgeHammerProperties(Properties properties, int baseMaterialDurability, int enchantmentValue) {
        return properties
                .durability((baseMaterialDurability * 20) / 3)
                .enchantable(enchantmentValue);
    }
}
