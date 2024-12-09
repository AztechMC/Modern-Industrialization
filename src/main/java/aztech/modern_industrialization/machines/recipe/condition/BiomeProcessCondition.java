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
package aztech.modern_industrialization.machines.recipe.condition;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.proxy.CommonProxy;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

public record BiomeProcessCondition(Either<ResourceKey<Biome>, TagKey<Biome>> biome) implements MachineProcessCondition {
    static final MapCodec<BiomeProcessCondition> CODEC = NeoForgeExtraCodecs
            .xor(
                    ResourceKey.codec(Registries.BIOME).fieldOf("biome"),
                    TagKey.codec(Registries.BIOME).fieldOf("tag"))
            .xmap(BiomeProcessCondition::new, BiomeProcessCondition::biome);
    static final StreamCodec<RegistryFriendlyByteBuf, BiomeProcessCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.either(
                    ResourceKey.streamCodec(Registries.BIOME),
                    ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.BIOME, rl), TagKey::location)),
            BiomeProcessCondition::biome,
            BiomeProcessCondition::new);

    @Override
    public boolean canProcessRecipe(Context context, MachineRecipe recipe) {
        var entityBiome = context.getLevel().getBiome(context.getBlockEntity().getBlockPos());
        return biome.map(entityBiome::is, entityBiome::is);
    }

    private static MutableComponent biomeId(ResourceLocation biomeLocation) {
        return Component.translatable(Util.makeDescriptionId("biome", biomeLocation));
    }

    @Override
    public void appendDescription(List<Component> list) {
        biome.ifLeft(rk -> {
            list.add(MIText.RequiresBiome.text(biomeId(rk.location())));
        }).ifRight(tag -> {
            var holderLookup = CommonProxy.INSTANCE.getClientPlayer().registryAccess();
            var biomeNames = holderLookup.lookupOrThrow(tag.registry()).get(tag).map(named -> {
                return named.stream()
                        .map(holder -> biomeId(holder.unwrapKey().orElseThrow().location()))
                        .reduce((a, b) -> a.append(", ").append(b))
                        .orElseThrow();
            }).orElse(Component.literal("???"));

            list.add(MIText.RequiresBiome.text(biomeNames));
        });
    }

    @Override
    public MapCodec<? extends MachineProcessCondition> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ? extends MachineProcessCondition> streamCodec() {
        return STREAM_CODEC;
    }
}
