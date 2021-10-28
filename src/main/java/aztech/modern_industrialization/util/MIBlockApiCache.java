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
package aztech.modern_industrialization.util;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Version of {@link BlockApiCache} that will only query APIs if the target
 * chunk is ticking.
 */
// TODO: consider PR'ing this to fabric
public class MIBlockApiCache<A, C> {
    public static <A, C> MIBlockApiCache<A, C> create(BlockApiLookup<A, C> lookup, ServerWorld world, BlockPos pos) {
        return new MIBlockApiCache<>(lookup, world, pos);
    }

    private final BlockApiCache<A, C> cache;
    private final ServerWorld world;
    private final BlockPos pos;

    private MIBlockApiCache(BlockApiLookup<A, C> lookup, ServerWorld world, BlockPos pos) {
        this.cache = BlockApiCache.create(lookup, world, pos);
        this.world = world;
        this.pos = pos;
    }

    @Nullable
    public A find(C context) {
        if (world.method_37117(pos)) {
            return cache.find(context);
        }
        return null;
    }
}
