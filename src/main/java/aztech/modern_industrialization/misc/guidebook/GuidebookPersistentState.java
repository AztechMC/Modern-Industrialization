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
package aztech.modern_industrialization.misc.guidebook;

import aztech.modern_industrialization.util.MISavedData;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class GuidebookPersistentState extends MISavedData {
    private static final Factory<GuidebookPersistentState> FACTORY = new Factory<>(GuidebookPersistentState::new, GuidebookPersistentState::fromNbt);
    private static final String NAME = "modern_industrialization_guidebook";
    private final Set<String> receivedPlayers;

    private GuidebookPersistentState(Set<String> receivedPlayers) {
        this.receivedPlayers = receivedPlayers;
    }

    private GuidebookPersistentState() {
        this(new HashSet<>());
    }

    public boolean hasPlayerReceivedGuidebook(Player player) {
        return receivedPlayers.contains(player.getStringUUID());
    }

    public void addPlayerReceivedGuidebook(Player player) {
        receivedPlayers.add(player.getStringUUID());
        setDirty();
    }

    public static GuidebookPersistentState fromNbt(CompoundTag tag) {
        Set<String> receivedPlayers = new HashSet<>();
        ListTag list = tag.getList("receivedPlayers", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); ++i) {
            receivedPlayers.add(list.getString(i));
        }
        return new GuidebookPersistentState(receivedPlayers);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (String receivedPlayer : receivedPlayers) {
            list.add(StringTag.valueOf(receivedPlayer));
        }
        tag.put("receivedPlayers", list);
        return tag;
    }

    public static GuidebookPersistentState get(MinecraftServer server) {
        ServerLevel world = server.getLevel(ServerLevel.OVERWORLD);
        return world.getDataStorage().computeIfAbsent(FACTORY, NAME);
    }
}
