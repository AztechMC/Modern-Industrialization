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
package aztech.modern_industrialization.test;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.machines.blockentities.GeneratorMachineBlockEntity;
import aztech.modern_industrialization.test.framework.MIGameTest;
import aztech.modern_industrialization.test.framework.MIGameTestHelper;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;

public class GeneratorTests {
    /**
     * Regression test for <a href="https://github.com/AztechMC/Modern-Industrialization/issues/1152">issue 1152</a>:
     * "EU is left in a hidden buffer when Diesel Generator runs out of fuel".
     */
    @MIGameTest
    public void testGeneratorEmptiesEuBuffer(MIGameTestHelper helper) {
        var generatorPos = new BlockPos(0, 1, 0);
        var lvDieselGeneratorBlock = BuiltInRegistries.BLOCK.getOptional(MI.id("lv_diesel_generator")).orElseThrow();
        helper.setBlock(generatorPos, lvDieselGeneratorBlock);

        var dieselGenerator = (GeneratorMachineBlockEntity) helper.getBlockEntity(generatorPos);

        try (var tx = Transaction.openOuter()) {
            long inserted = dieselGenerator.getInventory().fluidStorage.insert(MIFluids.BIODIESEL.variant(), 1, tx);
            helper.assertValueEqual(inserted, 1L, "inserted biodiesel");
            tx.commit();
        }

        // Should produce 64 per tick: 192 after 3 ticks and only 250 (the biodiesel value) after 4 ticks
        helper.startSequence()
                .thenIdle(3)
                .thenExecute(() -> helper.assertEnergy(generatorPos, 192, Direction.NORTH))
                .thenIdle(1)
                .thenExecute(() -> helper.assertEnergy(generatorPos, 250, Direction.NORTH))
                .thenSucceed();
    }
}
