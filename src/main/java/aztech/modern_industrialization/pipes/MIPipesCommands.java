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
package aztech.modern_industrialization.pipes;

import static net.minecraft.command.argument.BlockPosArgumentType.blockPos;
import static net.minecraft.command.argument.BlockPosArgumentType.getLoadedBlockPos;
import static net.minecraft.command.argument.IdentifierArgumentType.getIdentifier;
import static net.minecraft.command.argument.IdentifierArgumentType.identifier;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.impl.PipeNetworks;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class MIPipesCommands {
    private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> {
        return CommandSource.suggestIdentifiers(PipeNetworkType.getTypes().keySet().stream(), builder);
    };

    // @formatter:off
    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            if (!MIConfig.getConfig().enableDebugCommands) {
                return;
            }

            dispatcher.register(literal("mi")
                    .requires(source -> source.hasPermissionLevel(4))
                    .then(literal("pipes")
                            .then(argument("pos", blockPos())
                                    .then(literal("clear")
                                            .executes(ctx -> {
                                                return clearPipes(ctx.getSource(), getLoadedBlockPos(ctx, "pos"));
                                            })
                                    )
                                    .then(literal("add_ghost")
                                            .then(argument("pipe_type", identifier()).suggests(SUGGESTION_PROVIDER)
                                                    .executes(ctx -> {
                                                        return addGhostPipe(ctx.getSource(), getLoadedBlockPos(ctx, "pos"), getIdentifier(ctx, "pipe_type"));
                                                    })
                                            )
                                    )
                            )
                    )
            );
        });
    }
    // @formatter:on

    private static int clearPipes(ServerCommandSource src, BlockPos pos) {
        // Clear pipe block first (if possible, hopefully yes)
        if (src.getWorld().getBlockState(pos).isOf(MIPipes.BLOCK_PIPE)) {
            src.getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
        }

        var networks = PipeNetworks.get(src.getWorld());
        for (var type : PipeNetworkType.getTypes().values()) {
            var manager = networks.getManager(type);
            if (manager.hasNode(pos)) {
                manager.removeNode(pos);
                src.sendFeedback(new LiteralText("Successfully removed pipe of type %s at position %s.".formatted(type.getIdentifier(), pos)), true);
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int addGhostPipe(ServerCommandSource src, BlockPos pos, Identifier pipeType) throws CommandSyntaxException {
        PipeNetworkType type = PipeNetworkType.get(pipeType);
        if (type == null) {
            throw new SimpleCommandExceptionType(new LiteralText("Unknown pipe network type: " + pipeType)).create();
        }

        var networks = PipeNetworks.get(src.getWorld());
        var manager = networks.getManager(type);
        if (!manager.hasNode(pos)) {
            manager.addNode(type.getNodeCtor().get(), pos, MIPipes.INSTANCE.getPipeItem(type).defaultData);
            src.sendFeedback(new LiteralText("Successfully added pipe of type %s at position %s.".formatted(type.getIdentifier(), pos)), true);
        } else {
            src.sendFeedback(
                    new LiteralText("Failed to add pipe of type %s at position %s as it already existed.".formatted(type.getIdentifier(), pos)),
                    true);
        }

        return Command.SINGLE_SUCCESS;
    }
}
