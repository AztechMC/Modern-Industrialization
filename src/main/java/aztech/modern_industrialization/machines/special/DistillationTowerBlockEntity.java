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
package aztech.modern_industrialization.machines.special;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.multiblock.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.impl.multiblock.MultiblockShape;
import aztech.modern_industrialization.machines.impl.multiblock.MultiblockShapes;

public class DistillationTowerBlockEntity extends MultiblockMachineBlockEntity {
    private static final int MAX_OUTPUTS = 8; // max number of outputs
    private static final MultiblockShape[] SHAPES; // 0-th shape has one outputs, etc...
    private int outputs = 0; // current number of outputs, 0 means that the shape is invalid

    public DistillationTowerBlockEntity(MachineFactory factory) {
        super(factory, null);
    }

    @Override
    protected void matchShape() {
        outputs = 1;
        for (int h = 1; h < MAX_OUTPUTS; ++h) {
            if (SHAPES[h].matchShape(world, pos, facingDirection, linkedHatches, linkedStructureBlocks)) {
                outputs = h + 1;
            }
        }

        ready = SHAPES[outputs - 1].matchShape(world, pos, facingDirection, linkedHatches, linkedStructureBlocks);
        errorMessage = SHAPES[outputs - 1].getErrorMessage();
        if (!ready) {
            outputs = 0;
        }
    }

    @Override
    protected int getMaxFluidOutputs() {
        return outputs;
    }

    static {
        SHAPES = new MultiblockShape[MAX_OUTPUTS];

        MultiblockShape.Entry baseLayer = MultiblockShapes.or(MultiblockShapes.blockId(new MIIdentifier("clean_stainless_steel_machine_casing")),
                MultiblockShapes.hatch(MultiblockShapes.HATCH_FLAG_ENERGY_INPUT | MultiblockShapes.HATCH_FLAG_FLUID_INPUT));
        MultiblockShape.Entry outputLayer = MultiblockShapes.or(MultiblockShapes.blockId(new MIIdentifier("clean_stainless_steel_machine_casing")),
                MultiblockShapes.hatch(MultiblockShapes.HATCH_FLAG_FLUID_OUTPUT));

        for (int i = 0; i < MAX_OUTPUTS; ++i) {
            MultiblockShape s = new MultiblockShape();

            for (int x = -1; x <= 1; ++x) {
                for (int z = 0; z < 3; ++z) {
                    if (x != 0 || z != 0) {
                        s.addEntry(x, 0, z, baseLayer);
                    }
                    if (x != 0 || z != 1) {
                        for (int y = 1; y <= i + 1; ++y) {
                            s.addEntry(x, y, z, outputLayer);
                        }
                    }
                }
            }
            s.setMaxHatches(i + 3); // 1 input, 1 energy and i+1 outputs

            SHAPES[i] = s;
        }
    }
}
