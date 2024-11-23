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
package aztech.modern_industrialization.machines.multiblocks.structure;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.network.structure.StructureMisconfiguredBlocksPacket;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface StructureResult {
    default boolean isSuccess() {
        return false;
    }

    Component text();

    default void sendMessage(Player player) {
        player.sendSystemMessage(this.text());
    }

    default void send(Player player) {
        this.sendMessage(player);
        this.clearMisconfiguredBlocks(player);
    }

    default void sendMisconfiguredBlocks(Player player, List<BlockPos> positions) {
        if (player instanceof ServerPlayer serverPlayer) {
            StructureMisconfiguredBlocksPacket.misconfigured(positions).sendToClient(serverPlayer);
        }
    }

    default void clearMisconfiguredBlocks(Player player) {
        this.sendMisconfiguredBlocks(player, new ArrayList<>());
    }

    final class Unknown implements StructureResult {
        @Override
        public Component text() {
            return MIText.StructureMultiblockSaveFailUnknown.text().withStyle(ChatFormatting.RED);
        }
    }

    final class InvalidBounds implements StructureResult {
        @Override
        public Component text() {
            return MIText.StructureMultiblockSaveFailInvalidBounds.text().withStyle(ChatFormatting.RED);
        }
    }

    record MisconfiguredBlocks(List<BlockPos> positions) implements StructureResult {
        @Override
        public Component text() {
            return MIText.StructureMultiblockSaveFailMisconfiguredBlock.text().withStyle(ChatFormatting.RED);
        }

        @Override
        public void send(Player player) {
            this.sendMessage(player);
            this.sendMisconfiguredBlocks(player, positions);
        }
    }

    record TooManyControllers(List<BlockPos> positions) implements StructureResult {
        @Override
        public Component text() {
            return MIText.StructureMultiblockSaveFailTooManyControllers.text().withStyle(ChatFormatting.RED);
        }

        @Override
        public void send(Player player) {
            this.sendMessage(player);
            this.sendMisconfiguredBlocks(player, positions);
        }
    }

    final class NoController implements StructureResult {
        @Override
        public Component text() {
            return MIText.StructureMultiblockSaveFailNoController.text().withStyle(ChatFormatting.RED);
        }
    }

    final class NoHatches implements StructureResult {
        @Override
        public Component text() {
            return MIText.StructureMultiblockSaveFailNoHatches.text().withStyle(ChatFormatting.RED);
        }
    }

    record Success(ResourceLocation structureId, CompoundTag tag) implements StructureResult {
        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public Component text() {
            return MIText.StructureMultiblockSaveSuccess.text(structureId.toString());
        }
    }
}
