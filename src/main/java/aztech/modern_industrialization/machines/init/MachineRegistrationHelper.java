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
package aztech.modern_industrialization.machines.init;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.models.MachineModels;
import aztech.modern_industrialization.util.MobSpawning;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MachineRegistrationHelper {
    /**
     * Register a machine's block, block entity type and wrenchable tag.
     * 
     * @param id                Machine block id, for example "electric_macerator"
     * @param factory           The block entity constructor, with a BET parameter.
     * @param extraRegistrators A list of BET consumer used for API registration.
     */
    @SafeVarargs
    public static BlockEntityType<?> registerMachine(String englishName, String id, Function<BEP, BlockEntity> factory,
            Consumer<BlockEntityType<?>>... extraRegistrators) {
        BlockEntityType<?>[] bet = new BlockEntityType[1];
        BiFunction<BlockPos, BlockState, BlockEntity> ctor = (pos, state) -> factory.apply(new BEP(bet[0], pos, state));

        BlockDefinition<MachineBlock> blockDefinition = MIBlock.block(
                englishName,
                id,
                MIBlock.BlockDefinitionParams.of().withBlockConstructor(
                        (s) -> new MachineBlock(ctor, s)).noModel().isValidSpawn(MobSpawning.NO_SPAWN),
                MachineBlock.class);

        Block block = blockDefinition.asBlock();

        bet[0] = Registry.register(Registry.BLOCK_ENTITY_TYPE, new MIIdentifier(id),
                FabricBlockEntityTypeBuilder.create(ctor::apply, block).build(null));
        for (Consumer<BlockEntityType<?>> extraRegistrator : extraRegistrators) {
            extraRegistrator.accept(bet[0]);
        }

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            MachineModels.addMachineBer(bet[0], id);
        }

        return bet[0];
    }

}
