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

package aztech.modern_industrialization.stats;

import aztech.modern_industrialization.compat.argonauts.ArgonautsFacade;
import aztech.modern_industrialization.compat.ftbquests.FTBQuestsFacade;
import aztech.modern_industrialization.compat.ftbteams.FTBTeamsFacade;
import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public class PlayerStatistics {
    public static final PlayerStatistics DUMMY = new PlayerStatistics();

    public static final Codec<PlayerStatistics> CODEC = RecordCodecBuilder.create(
            instance ->
                    instance
                            .group(
                                    registryCodec(BuiltInRegistries.ITEM).fieldOf("usedItems").forGetter(s -> s.usedItems),
                                    registryCodec(BuiltInRegistries.ITEM).fieldOf("producedItems").forGetter(s -> s.producedItems),
                                    registryCodec(BuiltInRegistries.FLUID).fieldOf("usedFluids").forGetter(s -> s.usedFluids),
                                    registryCodec(BuiltInRegistries.FLUID).fieldOf("producedFluids").forGetter(s -> s.producedFluids),
                                    pendingCodec().fieldOf("pendingCraftedStats").forGetter(s -> s.pendingCraftedStats)
                            )
                            .apply(instance, PlayerStatistics::new));

    private PlayerStatisticsData data;
    @Nullable
    private UUID uuid;
    private final IdentityHashMap<Item, StatisticValue> usedItems;
    private final IdentityHashMap<Item, StatisticValue> producedItems;
    private final IdentityHashMap<Fluid, StatisticValue> usedFluids;
    private final IdentityHashMap<Fluid, StatisticValue> producedFluids;

    // Items produced while the player was offline... this is used to award vanilla stats when the player comes back online.
    private final Reference2LongMap<Item> pendingCraftedStats;

    private static final Set<UUID> uuidCache = new HashSet<>();

    PlayerStatistics() {
        this(new IdentityHashMap<>(), new IdentityHashMap<>(), new IdentityHashMap<>(), new IdentityHashMap<>(), new Reference2LongOpenHashMap<>());
    }

    PlayerStatistics(
            IdentityHashMap<Item, StatisticValue> usedItems,
            IdentityHashMap<Item, StatisticValue> producedItems,
            IdentityHashMap<Fluid, StatisticValue> usedFluids,
            IdentityHashMap<Fluid, StatisticValue> producedFluids,
            Reference2LongMap<Item> pendingCraftedStats) {
        this.usedItems = usedItems;
        this.producedItems = producedItems;
        this.usedFluids = usedFluids;
        this.producedFluids = producedFluids;
        this.pendingCraftedStats = pendingCraftedStats;
    }

    void setDataAndUuid(PlayerStatisticsData data, UUID uuid) {
        this.data = data;
        this.uuid = uuid;
    }

    public void addUsedItems(ItemLike what, long amount) {
        usedItems.computeIfAbsent(what.asItem(), i -> new StatisticValue()).add(amount);
    }

    public void addProducedItems(Level level, ItemLike what, long amount) {
        var item = what.asItem();
        producedItems.computeIfAbsent(item, i -> new StatisticValue()).add(amount);

        if (uuid != null) {
            FTBQuestsFacade.INSTANCE.addCompleted(uuid, item, amount);

            awardStat(level, what, amount);

            // Make sure we only award the stats to other players once even if they have both FTB Teams and Argonauts.
            uuidCache.clear();
            uuidCache.addAll(FTBTeamsFacade.INSTANCE.getOtherPlayersInTeam(uuid));
            uuidCache.addAll(ArgonautsFacade.INSTANCE.getOtherPlayersInGuild(level, uuid));

            for (var uuid : uuidCache) {
                data.get(uuid).awardStat(level, what, amount);
            }
            uuidCache.clear();
        }
    }

    public void addUsedFluids(Fluid what, long amount) {
        usedFluids.computeIfAbsent(what, i -> new StatisticValue()).add(amount);
    }

    public void addProducedFluids(Fluid what, long amount) {
        producedFluids.computeIfAbsent(what, i -> new StatisticValue()).add(amount);
    }

    private void awardStat(Level level, ItemLike what, long amount) {
        Objects.requireNonNull(uuid);

        var player = level.getPlayerByUUID(uuid);

        if (player != null) {
            player.awardStat(Stats.ITEM_CRAFTED.get(what.asItem()), Ints.saturatedCast(amount));
        } else {
            pendingCraftedStats.mergeLong(what.asItem(), amount, Long::sum);
        }
    }

    public void onPlayerJoin(ServerPlayer player) {
        if (!pendingCraftedStats.isEmpty()) {
            for (var entry : pendingCraftedStats.reference2LongEntrySet()) {
                player.awardStat(Stats.ITEM_CRAFTED.get(entry.getKey()), Ints.saturatedCast(entry.getLongValue()));
            }

            pendingCraftedStats.clear();
        }
    }

    private static <T> Codec<IdentityHashMap<T, StatisticValue>> registryCodec(Registry<T> registry) {
        return Codec.unboundedMap(registry.byNameCodec(), StatisticValue.CODEC)
                // Ignore errored values
                .promotePartial(err -> {})
                .xmap(IdentityHashMap::new, m -> m);
    }

    private static Codec<Reference2LongMap<Item>> pendingCodec() {
        return Codec.unboundedMap(BuiltInRegistries.ITEM.byNameCodec(), Codec.LONG)
                // Ignore errored values
                .promotePartial(err -> {})
                .xmap(Reference2LongOpenHashMap::new, m -> m);
    }
}
