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
package aztech.modern_industrialization.network.structure;

import aztech.modern_industrialization.blocks.structure.controller.StructureControllerBounds;
import aztech.modern_industrialization.blocks.structure.controller.StructureControllerMode;
import aztech.modern_industrialization.blocks.structure.controller.StructureMultiblockControllerBlockEntity;
import aztech.modern_industrialization.network.BasePacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public record StructureUpdateControllerPacket(BlockPos pos, StructureControllerMode mode, String inputId, String inputCasing,
        StructureControllerBounds bounds, boolean showBounds, boolean includeBlockEntities)
        implements BasePacket {

    public static final StreamCodec<ByteBuf, StructureUpdateControllerPacket> STREAM_CODEC = NeoForgeStreamCodecs.composite(
            BlockPos.STREAM_CODEC,
            StructureUpdateControllerPacket::pos,
            ByteBufCodecs.idMapper((i) -> StructureControllerMode.values()[i], Enum::ordinal),
            StructureUpdateControllerPacket::mode,
            ByteBufCodecs.STRING_UTF8,
            StructureUpdateControllerPacket::inputId,
            ByteBufCodecs.STRING_UTF8,
            StructureUpdateControllerPacket::inputCasing,
            ByteBufCodecs.fromCodec(StructureControllerBounds.CODEC),
            StructureUpdateControllerPacket::bounds,
            ByteBufCodecs.BOOL,
            StructureUpdateControllerPacket::showBounds,
            ByteBufCodecs.BOOL,
            StructureUpdateControllerPacket::includeBlockEntities,
            StructureUpdateControllerPacket::new);

    @Override
    public void handle(Context ctx) {
        ctx.assertOnServer();

        Player player = ctx.getPlayer();
        Level level = player.level();

        if (!player.canUseGameMasterBlocks()) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof StructureMultiblockControllerBlockEntity controller) {
            controller.setMode(mode);
            controller.setInputId(inputId);
            controller.setInputCasing(inputCasing);
            controller.setBounds(bounds);
            controller.setShowBounds(showBounds);
            controller.setIncludeBlockEntities(includeBlockEntities);

            controller.sync();
            controller.setChanged();

            if (player instanceof ServerPlayer serverPlayer && controller.isConfigurationValid()) {
                StructureMisconfiguredBlocksPacket.forget(controller.getBlockPos()).sendToClient(serverPlayer);
            }
        }
    }
}
