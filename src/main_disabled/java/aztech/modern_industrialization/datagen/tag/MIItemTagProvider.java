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

import appeng.api.features.P2PTunnelAttunement;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MITags;
import aztech.modern_industrialization.compat.ae2.MIAEAddon;
import aztech.modern_industrialization.machines.blockentities.ReplicatorMachineBlockEntity;
import aztech.modern_industrialization.materials.MIMaterials;
import aztech.modern_industrialization.materials.part.MIParts;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class MIItemTagProvider extends FabricTagProvider.ItemTagProvider {
    private final boolean runtimeDatagen;

    public MIItemTagProvider(FabricDataOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture, boolean runtimeDatagen) {
        super(packOutput, registriesFuture);
        this.runtimeDatagen = runtimeDatagen;
    }

    @Override
    protected FabricTagBuilder tag(TagKey<Item> tag) {
        return getOrCreateTagBuilder(tag);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        generatedConventionTag();

        for (var entry : TagsToGenerate.tagToItemMap.entrySet()) {
            boolean optional = TagsToGenerate.optionalTags.contains(entry.getKey());
            var tagId = new ResourceLocation(entry.getKey());
            for (var item : entry.getValue()) {
                if (optional) {
                    tag(key(tagId)).addOptional(BuiltInRegistries.ITEM.getKey(item));
                } else {
                    tag(key(tagId)).add(item);
                }
            }
        }

        for (var entry : TagsToGenerate.tagToBeAddedToAnotherTag.entrySet()) {
            var tagId = new ResourceLocation(entry.getKey());
            for (var tag : entry.getValue()) {
                tag(key(tagId)).forceAddTag(key(tag));
            }
        }

        for (var goldenItem : MIMaterials.GOLD.getParts().values()) {
            String namespace = goldenItem.getItemId().split(":")[0];
            if (!Objects.equals(namespace, "minecraft")) {
                tag(ItemTags.PIGLIN_LOVED).add(goldenItem.asItem());
            }
        }

        tag(ReplicatorMachineBlockEntity.BLACKLISTED)
                .add(Items.BUNDLE, MIItem.PORTABLE_STORAGE_UNIT.asItem())
                .forceAddTag(ConventionalItemTags.SHULKER_BOXES)
                .addTag(MITags.TANKS)
                .addTag(MITags.BARRELS);

        if (FabricLoader.getInstance().isModLoaded("ae2") && !runtimeDatagen) {
            tag(P2PTunnelAttunement.getAttunementTag(MIAEAddon.ENERGY_P2P_TUNNEL))
                    .add(MIMaterials.SUPERCONDUCTOR.getPart(MIParts.CABLE).asItem());
        }
    }

    private static TagKey<Item> key(ResourceLocation id) {
        return TagKey.create(BuiltInRegistries.ITEM.key(), id);
    }

    private static TagKey<Item> key(String id) {
        return key(new ResourceLocation(id));
    }

    private void generatedConventionTag() {
        tag(key("c:iron_nuggets")).add(Items.IRON_NUGGET);
        tag(key("c:iron_blocks")).add(Items.IRON_BLOCK);
        tag(key("c:iron_ores")).forceAddTag(ItemTags.IRON_ORES);

        tag(key("c:copper_blocks")).add(Items.COPPER_BLOCK);
        tag(key("c:copper_ores")).forceAddTag(ItemTags.COPPER_ORES);

        tag(key("c:gold_nuggets")).add(Items.GOLD_NUGGET);
        tag(key("c:gold_blocks")).add(Items.GOLD_BLOCK);
        tag(key("c:gold_ores")).forceAddTag(ItemTags.GOLD_ORES);

        tag(key("c:coal_blocks")).add(Items.COAL_BLOCK);
        tag(key("c:coal_ores")).forceAddTag(ItemTags.COAL_ORES);

        tag(key("c:redstone_blocks")).add(Items.REDSTONE_BLOCK);
        tag(key("c:redstone_ores")).forceAddTag(ItemTags.REDSTONE_ORES);

        tag(key("c:emerald_blocks")).add(Items.EMERALD_BLOCK);
        tag(key("c:emerald_ores")).forceAddTag(ItemTags.EMERALD_ORES);

        tag(key("c:diamond_blocks")).add(Items.DIAMOND_BLOCK);
        tag(key("c:diamond_ores")).forceAddTag(ItemTags.DIAMOND_ORES);

        tag(key("c:lapis_blocks")).add(Items.LAPIS_BLOCK);
        tag(key("c:lapis_ores")).forceAddTag(ItemTags.LAPIS_ORES);

        tag(key("c:quartz_ores")).add(Items.NETHER_QUARTZ_ORE);

        tag(key("c:wooden_barrels")).add(Items.BARREL);
    }
}
