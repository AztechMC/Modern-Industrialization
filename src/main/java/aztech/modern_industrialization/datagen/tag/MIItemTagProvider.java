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
import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MITags;
import aztech.modern_industrialization.compat.ae2.MIAEAddon;
import aztech.modern_industrialization.machines.blockentities.ReplicatorMachineBlockEntity;
import aztech.modern_industrialization.materials.MIMaterials;
import aztech.modern_industrialization.materials.part.MIParts;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class MIItemTagProvider extends ItemTagsProvider {
    private final boolean runtimeDatagen;

    public MIItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
            @Nullable ExistingFileHelper existingFileHelper, boolean runtimeDatagen) {
        super(output, lookupProvider, CompletableFuture.completedFuture(TagLookup.empty()), MI.ID, existingFileHelper);
        this.runtimeDatagen = runtimeDatagen;
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        generatedConventionTag();

        for (var entry : TagsToGenerate.tagToItemMap.entrySet()) {
            boolean optional = TagsToGenerate.optionalTags.contains(entry.getKey());
            var tagId = new ResourceLocation(entry.getKey());
            for (var item : entry.getValue()) {
                if (optional) {
                    tag(key(tagId)).addOptional(BuiltInRegistries.ITEM.getKey(item.asItem()));
                } else {
                    tag(key(tagId)).add(item.asItem());
                }
            }
        }

        for (var entry : TagsToGenerate.tagToBeAddedToAnotherTag.entrySet()) {
            var tagId = new ResourceLocation(entry.getKey());
            for (var tag : entry.getValue()) {
                tag(key(tagId)).addTag(key(tag));
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
                .addTag(MITags.SHULKER_BOXES)
                .addTag(MITags.TANKS)
                .addTag(MITags.BARRELS);

        // Have no idea why there is such a tag but go add it
        tag(Tags.Items.ORES_QUARTZ).add(BuiltInRegistries.ITEM.get(new MIIdentifier("quartz_ore")));

        tag(key("forge:tools/shears")).add(MIItem.DIESEL_CHAINSAW.asItem());
        tag(MITags.WRENCHES).add(MIItem.WRENCH.asItem());
        tag(ItemTags.AXES).add(MIItem.DIESEL_CHAINSAW.asItem());
        tag(ItemTags.HOES).add(MIItem.DIESEL_CHAINSAW.asItem());
        tag(ItemTags.PICKAXES).add(MIItem.STEAM_MINING_DRILL.asItem(), MIItem.DIESEL_MINING_DRILL.asItem());
        tag(ItemTags.SHOVELS).add(MIItem.STEAM_MINING_DRILL.asItem(), MIItem.DIESEL_MINING_DRILL.asItem());
        tag(ItemTags.SWORDS).add(MIItem.DIESEL_CHAINSAW.asItem());

        tag(ItemTags.COALS).add(ResourceKey.create(Registries.ITEM, MI.id("lignite_coal")));

        if (ModList.get().isLoaded("ae2") && !runtimeDatagen) {
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
        tag(MITags.SHULKER_BOXES)
                .add(Items.SHULKER_BOX)
                .add(Items.WHITE_SHULKER_BOX)
                .add(Items.ORANGE_SHULKER_BOX)
                .add(Items.MAGENTA_SHULKER_BOX)
                .add(Items.LIGHT_BLUE_SHULKER_BOX)
                .add(Items.YELLOW_SHULKER_BOX)
                .add(Items.LIME_SHULKER_BOX)
                .add(Items.PINK_SHULKER_BOX)
                .add(Items.GRAY_SHULKER_BOX)
                .add(Items.LIGHT_GRAY_SHULKER_BOX)
                .add(Items.CYAN_SHULKER_BOX)
                .add(Items.PURPLE_SHULKER_BOX)
                .add(Items.BLUE_SHULKER_BOX)
                .add(Items.BROWN_SHULKER_BOX)
                .add(Items.GREEN_SHULKER_BOX)
                .add(Items.RED_SHULKER_BOX)
                .add(Items.BLACK_SHULKER_BOX);
    }
}
