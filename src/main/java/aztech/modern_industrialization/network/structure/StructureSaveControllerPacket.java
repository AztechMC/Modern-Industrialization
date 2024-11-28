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

import aztech.modern_industrialization.blocks.structure.controller.StructureMultiblockControllerBlock;
import aztech.modern_industrialization.blocks.structure.controller.StructureMultiblockControllerBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.structure.MIStructureTemplateManager;
import aztech.modern_industrialization.machines.multiblocks.structure.StructureResult;
import aztech.modern_industrialization.network.BasePacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public record StructureSaveControllerPacket(BlockPos pos) implements BasePacket {

    public static final StreamCodec<ByteBuf, StructureSaveControllerPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            StructureSaveControllerPacket::pos,
            StructureSaveControllerPacket::new);

    @Override
    public void handle(Context ctx) {
        ctx.assertOnServer();

        Player player = ctx.getPlayer();
        Level level = player.level();

        if (!player.canUseGameMasterBlocks()) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof StructureMultiblockControllerBlockEntity controller) {
            ResourceLocation id = controller.getId();
            var result = MIStructureTemplateManager.fromWorld(id,
                    level, pos, state.getValue(StructureMultiblockControllerBlock.FACING),
                    controller.getCasing(), controller.getBounds(), controller.includeBlockEntities());
            if (result instanceof StructureResult.Success success) {
                if (MIStructureTemplateManager.save(id, success.template())) {
                    MIStructureTemplateManager.register(id, success.template());
                } else {
                    result = new StructureResult.Unknown();
                }
            }
            result.send(player);
        }
    }
}
