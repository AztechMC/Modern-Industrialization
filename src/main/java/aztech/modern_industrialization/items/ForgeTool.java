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
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public class ForgeTool extends TieredItem {

    public static final TagKey<Item> TAG = TagKey.create(BuiltInRegistries.ITEM.key(), MI.id("forge_hammer_tools"));

    public ForgeTool(Tier material, Properties p) {
        super(forgeHammerMaterial(material), p.stacksTo(1));
        TagsToGenerate.generateTag(TAG, this, "Forge Hammer Tools");
    }

    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }

    private static Tier forgeHammerMaterial(Tier normalTier) {
        return new Tier() {
            @Override
            public int getUses() {
                return (normalTier.getUses() * 20) / 3;
            }

            @Override
            public float getSpeed() {
                return normalTier.getSpeed();
            }

            @Override
            public float getAttackDamageBonus() {
                return normalTier.getAttackDamageBonus();
            }

            @Override
            public TagKey<Block> getIncorrectBlocksForDrops() {
                return normalTier.getIncorrectBlocksForDrops();
            }

            @Override
            public int getEnchantmentValue() {
                return normalTier.getEnchantmentValue();
            }

            @Override
            public Ingredient getRepairIngredient() {
                return normalTier.getRepairIngredient();
            }

            @Override
            public String toString() {
                return normalTier.toString().toLowerCase(Locale.ROOT) + "_forge_tool";
            }
        };

    }

    public static Tier STEEL = new Tier() {
        @Override
        public int getUses() {
            return 650;
        }

        @Override
        public float getSpeed() {
            return 7.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 2.5F;
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return BlockTags.create(MI.id("incorrect_for_steel_tool")); // this probably doesn't matter...
        }

        @Override
        public int getEnchantmentValue() {
            return 16;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(TAG);
        }

        @Override
        public String toString() {
            return "modern_industrialization:steel";
        }
    };
}
