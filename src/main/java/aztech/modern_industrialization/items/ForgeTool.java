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

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.util.ResourceUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

public class ForgeTool extends ToolItem {

    public final String id;
    public static final MIIdentifier TAG = new MIIdentifier("forge_hammer_tools");

    public ForgeTool(ToolMaterial material, String id) {
        super(material, new FabricItemSettings().maxCount(1).group(ModernIndustrialization.ITEM_GROUP));
        this.id = id;
        MIItem.items.put(id, this);
        MIItem.handhelds.add(id);
        ResourceUtil.appendToItemTag(TAG, new MIIdentifier(id));
    }

    public String getPath() {
        return "modern_industrialization:" + id;
    }

    public static ToolMaterial STEEL = new ToolMaterial() {

        private static Identifier TAG = new Identifier("c:steel_ingots");

        @Override
        public int getDurability() {
            return 650;
        }

        @Override
        public float getMiningSpeedMultiplier() {
            return 7.0F;
        }

        @Override
        public float getAttackDamage() {
            return 2.5F;
        }

        @Override
        public int getMiningLevel() {
            return 2;
        }

        @Override
        public int getEnchantability() {
            return 16;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.fromTag(TagFactory.ITEM.create(TAG));
        }
    };
}
