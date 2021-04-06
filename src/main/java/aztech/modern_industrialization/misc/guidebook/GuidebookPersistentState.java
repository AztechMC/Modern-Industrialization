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

import java.util.*;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

public class GuidebookPersistentState extends PersistentState {
    private static final String NAME = "modern_industrialization_guidebook";
    private final Set<String> receivedPlayers = new HashSet<>();

    private GuidebookPersistentState() {
        super(NAME);
    }

    public boolean hasPlayerReceivedGuidebook(PlayerEntity player) {
        return receivedPlayers.contains(player.getUuidAsString());
    }

    public void addPlayerReceivedGuidebook(PlayerEntity player) {
        receivedPlayers.add(player.getUuidAsString());
        markDirty();
    }

    @Override
    public void fromTag(CompoundTag tag) {
        receivedPlayers.clear();
        ListTag list = tag.getList("receivedPlayers", NbtType.STRING);
        for (int i = 0; i < list.size(); ++i) {
            receivedPlayers.add(list.getString(i));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        ListTag list = new ListTag();
        for (String receivedPlayer : receivedPlayers) {
            list.add(StringTag.of(receivedPlayer));
        }
        tag.put("receivedPlayers", list);
        return tag;
    }

    public static GuidebookPersistentState get(MinecraftServer server) {
        ServerWorld world = server.getWorld(ServerWorld.OVERWORLD);
        return world.getPersistentStateManager().getOrCreate(GuidebookPersistentState::new, NAME);
    }
}
