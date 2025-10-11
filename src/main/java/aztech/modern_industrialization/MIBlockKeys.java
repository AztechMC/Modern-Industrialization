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

package aztech.modern_industrialization;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

/**
 * Holder for some keys that are needed before the block is even created.
 */
public class MIBlockKeys {
    public static final ResourceKey<Block> BASIC_MACHINE_HULL = block("basic_machine_hull");
    public static final ResourceKey<Block> ADVANCED_MACHINE_HULL = block("advanced_machine_hull");
    public static final ResourceKey<Block> TURBO_MACHINE_HULL = block("turbo_machine_hull");
    public static final ResourceKey<Block> HIGHLY_ADVANCED_MACHINE_HULL = block("highly_advanced_machine_hull");
    public static final ResourceKey<Block> QUANTUM_MACHINE_HULL = block("quantum_machine_hull");

    private static ResourceKey<Block> block(String path) {
        return ResourceKey.create(Registries.BLOCK, MI.id(path));
    }
}
