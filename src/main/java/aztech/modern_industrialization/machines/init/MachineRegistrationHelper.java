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
import aztech.modern_industrialization.MIRegistries;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.datagen.model.MachineModelsToGenerate;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.util.MobSpawning;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
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
    public static Supplier<BlockEntityType<?>> registerMachine(String englishName, String id,
            Function<BEP, MachineBlockEntity> factory,
            Consumer<BlockEntityType<?>>... extraRegistrators) {
        BlockEntityType<?>[] bet = new BlockEntityType[1];
        BiFunction<BlockPos, BlockState, MachineBlockEntity> ctor = (pos, state) -> factory.apply(new BEP(bet[0], pos, state));

        BlockDefinition<MachineBlock> blockDefinition = MIBlock.block(
                englishName,
                id,
                MIBlock.BlockDefinitionParams.defaultStone()
                        .sortOrder(SortOrder.MACHINES)
                        .withBlockConstructor((s) -> new MachineBlock(ctor, s))
                        .withModel((block, gen) -> {
                            // Model generation is handled in the model provider already.
                        })
                        .isValidSpawn(MobSpawning.NO_SPAWN));

        return MIRegistries.BLOCK_ENTITIES.register(id, () -> {
            Block block = blockDefinition.asBlock();

            bet[0] = BlockEntityType.Builder.of(ctor::apply, block).build(null);

            for (Consumer<BlockEntityType<?>> extraRegistrator : extraRegistrators) {
                extraRegistrator.accept(bet[0]);
            }

            return bet[0];
        });
    }

    @SuppressWarnings("IfCanBeSwitch")
    public static void addMachineModel(String tier, String id, String machineType, boolean frontOverlay, boolean topOverlay, boolean sideOverlay) {
        MachineCasing defaultCasing;
        if (tier.equals("bronze")) {
            defaultCasing = MachineCasings.BRONZE;
        } else if (tier.equals("steel")) {
            defaultCasing = MachineCasings.STEEL;
        } else if (tier.equals("electric")) {
            defaultCasing = CableTier.LV.casing;
        } else {
            throw new RuntimeException("Invalid tier: " + tier);
        }
        addMachineModel(id, machineType, defaultCasing, frontOverlay, topOverlay, sideOverlay);
    }

    public static void addMachineModel(String id, String overlayFolder, MachineCasing defaultCasing, boolean frontOverlay, boolean topOverlay,
            boolean sideOverlay) {
        addMachineModel(id, overlayFolder, defaultCasing, frontOverlay, topOverlay, sideOverlay, true);
    }

    public static void addMachineModel(String id, String overlayFolder, MachineCasing defaultCasing, boolean frontOverlay, boolean topOverlay,
            boolean sideOverlay, boolean hasActive) {
        MachineModelsToGenerate.register(id, defaultCasing, overlayFolder, frontOverlay, topOverlay, sideOverlay, hasActive);
    }

    public static void addModelsForTiers(String name, boolean frontOverlay, boolean topOverlay, boolean sideOverlay, String... tiers) {
        for (String tier : tiers) {
            addMachineModel(tier, tier + "_" + name, name, frontOverlay, topOverlay, sideOverlay);
        }
    }
}
