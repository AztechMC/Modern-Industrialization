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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

public class PlayerStatisticsData extends SavedData {
    private final Map<UUID, PlayerStatistics> stats = new HashMap<>();

    private PlayerStatisticsData(CompoundTag tag) {
        for (var key : tag.getAllKeys()) {
            stats.put(UUID.fromString(key), new PlayerStatistics(tag.getCompound(key)));
        }
    }

    private PlayerStatisticsData() {
    }

    public PlayerStatistics get(UUID uuid) {
        Objects.requireNonNull(uuid);
        return stats.computeIfAbsent(uuid, u -> new PlayerStatistics());
    }

    public PlayerStatistics get(Player player) {
        return get(player.getUUID());
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        for (var entry : stats.entrySet()) {
            tag.put(entry.getKey().toString(), entry.getValue().toTag());
        }
        return tag;
    }

    private static final String NAME = "modern_industrialization_player_stats";

    public static PlayerStatisticsData get(MinecraftServer server) {
        var overworld = server.getLevel(ServerLevel.OVERWORLD);
        Objects.requireNonNull(overworld, "Couldn't find overworld");
        return overworld.getDataStorage().computeIfAbsent(PlayerStatisticsData::new, PlayerStatisticsData::new, NAME);
    }
}
