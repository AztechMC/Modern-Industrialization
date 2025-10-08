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
package aztech.modern_industrialization.test.framework;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.blocks.storage.tank.creativetank.CreativeTankBlockEntity;
import aztech.modern_industrialization.materials.MIMaterials;
import aztech.modern_industrialization.materials.Material;
import aztech.modern_industrialization.materials.part.MIParts;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

public class MIGameTestHelper extends GameTestHelper {
    public MIGameTestHelper(GameTestInfo testInfo) {
        super(testInfo);
    }

    public void creativeTank(BlockPos pos, Fluid fluid) {
        setBlock(pos, MIBlock.CREATIVE_TANK.get());
        var tank = (CreativeTankBlockEntity) getBlockEntity(pos);
        tank.setFluid(FluidVariant.of(fluid));
    }

    public void emptyTank(BlockPos pos) {
        emptyTank(pos, MIMaterials.IRIDIUM);
    }

    public void emptyTank(BlockPos pos, Material material) {
        setBlock(pos, material.getPart(MIParts.TANK).asBlock());
    }

    public void assertAir(BlockPos pos) {
        assertBlockPresent(Blocks.AIR, pos);
    }

    public void pipe(BlockPos pos, PipeNetworkType type, Consumer<? super PipeBuilder> setup) {
        assertAir(pos);
        // Mimic logic inside of PipeItem
        setBlock(pos, MIPipes.BLOCK_PIPE.get());
        var pipeBe = (PipeBlockEntity) getBlockEntity(pos);
        var item = MIPipes.INSTANCE.getPipeItem(type);
        pipeBe.addPipe(type, item.defaultData);
        getLevel().blockUpdated(absolutePos(pos), Blocks.AIR);
        setup.accept(new PipeBuilder(this, pipeBe, type));
    }

    public PipeNetworkNode getPipeNode(BlockPos pos, PipeNetworkType type) {
        var pipeBe = (PipeBlockEntity) getBlockEntity(pos);
        for (var node : pipeBe.getNodes()) {
            if (node.getType() == type) {
                return node;
            }
        }
        fail("Could not find pipe node " + type.getIdentifier(), pos);
        throw new RuntimeException("Unreachable!");
    }

    public void assertFluid(BlockPos pos, Fluid fluid, int amount) {
        var fluidHandler = getLevel().getCapability(Capabilities.FluidHandler.BLOCK, absolutePos(pos), null);
        if (fluidHandler == null) {
            fail("Could not find fluid handler", pos);
        }
        boolean foundAny = false;
        var fluidName = BuiltInRegistries.FLUID.getKey(fluid);
        for (int i = 0; i < fluidHandler.getTanks(); ++i) {
            var stack = fluidHandler.getFluidInTank(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(fluid) && stack.getAmount() == amount) {
                foundAny = true;
                continue;
            }
            fail("Expected fluid %s and amount %d, got %s".formatted(fluidName, amount, stack), pos);
        }
        if (!foundAny) {
            fail("Expected fluid %s and amount %d, got nothing".formatted(fluidName, amount), pos);
        }
    }

    public void assertNoFluid(BlockPos pos) {
        var fluidHandler = getLevel().getCapability(Capabilities.FluidHandler.BLOCK, absolutePos(pos), null);
        if (fluidHandler == null) {
            fail("Could not find fluid handler", pos);
        }
        for (int i = 0; i < fluidHandler.getTanks(); ++i) {
            var stack = fluidHandler.getFluidInTank(i);
            if (!stack.isEmpty()) {
                fail("Expected no fluid, got %s".formatted(stack), pos);
            }
        }
    }

    public void assertEnergy(BlockPos pos, long energy, @Nullable Direction side) {
        var miEnergyHandler = getLevel().getCapability(EnergyApi.SIDED, absolutePos(pos), side);
        if (miEnergyHandler == null) {
            fail("Could not find energy handler", pos);
        }
        long storedEnergy = miEnergyHandler.getAmount();
        if (storedEnergy != energy) {
            fail("Expected energy to be " + energy + ", was " + storedEnergy, pos);
        }
    }
}
