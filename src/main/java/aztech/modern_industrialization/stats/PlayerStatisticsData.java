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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class PlayerStatisticsData extends SavedData {
    private static final Codec<PlayerStatisticsData> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, PlayerStatistics.CODEC)
            .xmap(m -> new PlayerStatisticsData(new HashMap<>(m)), psd -> psd.stats);
    private static final SavedDataType<PlayerStatisticsData> TYPE = new SavedDataType<>(
            "modern_industrialization_player_stats",
            PlayerStatisticsData::new,
            CODEC);

    public static PlayerStatisticsData get(MinecraftServer server) {
        var overworld = server.getLevel(ServerLevel.OVERWORLD);
        Objects.requireNonNull(overworld, "Couldn't find overworld");
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    private final HashMap<UUID, PlayerStatistics> stats;

    private PlayerStatisticsData(HashMap<UUID, PlayerStatistics> stats) {
        this.stats = stats;
    }

    private PlayerStatisticsData() {
        this(new HashMap<>());
    }

    public PlayerStatistics get(UUID uuid) {
        Objects.requireNonNull(uuid);
        var ret = stats.computeIfAbsent(uuid, u -> new PlayerStatistics());
        ret.setDataAndUuid(this, uuid);
        return ret;
    }

    public PlayerStatistics get(Player player) {
        return get(player.getUUID());
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
