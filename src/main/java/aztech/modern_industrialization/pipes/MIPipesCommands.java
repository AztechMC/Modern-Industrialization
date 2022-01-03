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

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.ResourceLocationArgument.getId;
import static net.minecraft.commands.arguments.ResourceLocationArgument.id;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.blockPos;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.getLoadedBlockPos;

import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.impl.PipeNetworks;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public class MIPipesCommands {
    private static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = (context, builder) -> {
        return SharedSuggestionProvider.suggestResource(PipeNetworkType.getTypes().keySet().stream(), builder);
    };

    // @formatter:off
    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            if (!MIConfig.getConfig().enableDebugCommands) {
                return;
            }

            dispatcher.register(literal("mi")
                    .requires(source -> source.hasPermission(4))
                    .then(literal("pipes")
                            .then(argument("pos", blockPos())
                                    .then(literal("clear")
                                            .executes(ctx -> {
                                                return clearPipes(ctx.getSource(), getLoadedBlockPos(ctx, "pos"));
                                            })
                                    )
                                    .then(literal("add_ghost")
                                            .then(argument("pipe_type", id()).suggests(SUGGESTION_PROVIDER)
                                                    .executes(ctx -> {
                                                        return addGhostPipe(ctx.getSource(), getLoadedBlockPos(ctx, "pos"), getId(ctx, "pipe_type"));
                                                    })
                                            )
                                    )
                            )
                    )
            );
        });
    }
    // @formatter:on

    private static int clearPipes(CommandSourceStack src, BlockPos pos) {
        // Clear pipe block first (if possible, hopefully yes)
        if (src.getLevel().getBlockState(pos).is(MIPipes.BLOCK_PIPE)) {
            src.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }

        var networks = PipeNetworks.get(src.getLevel());
        for (var type : PipeNetworkType.getTypes().values()) {
            var manager = networks.getManager(type);
            if (manager.hasNode(pos)) {
                manager.removeNode(pos);
                src.sendSuccess(new TextComponent("Successfully removed pipe of type %s at position %s.".formatted(type.getIdentifier(), pos)), true);
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int addGhostPipe(CommandSourceStack src, BlockPos pos, ResourceLocation pipeType) throws CommandSyntaxException {
        PipeNetworkType type = PipeNetworkType.get(pipeType);
        if (type == null) {
            throw new SimpleCommandExceptionType(new TextComponent("Unknown pipe network type: " + pipeType)).create();
        }

        var networks = PipeNetworks.get(src.getLevel());
        var manager = networks.getManager(type);
        if (!manager.hasNode(pos)) {
            manager.addNode(type.getNodeCtor().get(), pos, MIPipes.INSTANCE.getPipeItem(type).defaultData);
            src.sendSuccess(new TextComponent("Successfully added pipe of type %s at position %s.".formatted(type.getIdentifier(), pos)), true);
        } else {
            src.sendSuccess(
                    new TextComponent("Failed to add pipe of type %s at position %s as it already existed.".formatted(type.getIdentifier(), pos)),
                    true);
        }

        return Command.SINGLE_SUCCESS;
    }
}
