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

package aztech.modern_industrialization.guidebook;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class GuidebookPersistentState extends SavedData {
    private static final Codec<GuidebookPersistentState> CODEC = RecordCodecBuilder.create(
            i -> i.group(
                    UUIDUtil.CODEC_SET.fieldOf("receivedPlayers").forGetter(s -> s.receivedPlayers))
                    .apply(i, GuidebookPersistentState::new));
    private static final SavedDataType<GuidebookPersistentState> TYPE = new SavedDataType<>(
            "modern_industrialization_guidebook",
            GuidebookPersistentState::new,
            CODEC);

    private final Set<UUID> receivedPlayers;

    private GuidebookPersistentState(Set<UUID> receivedPlayers) {
        this.receivedPlayers = receivedPlayers;
    }

    private GuidebookPersistentState() {
        this(new HashSet<>());
    }

    public boolean hasPlayerReceivedGuidebook(Player player) {
        return receivedPlayers.contains(player.getUUID());
    }

    public void addPlayerReceivedGuidebook(Player player) {
        receivedPlayers.add(player.getUUID());
        setDirty();
    }

    public static GuidebookPersistentState get(MinecraftServer server) {
        ServerLevel world = server.getLevel(ServerLevel.OVERWORLD);
        return world.getDataStorage().computeIfAbsent(TYPE);
    }
}
