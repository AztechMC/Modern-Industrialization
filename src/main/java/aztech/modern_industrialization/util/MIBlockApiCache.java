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

import aztech.modern_industrialization.api.ICacheableApiHost;
import aztech.modern_industrialization.mixin.BlockApiCacheAccessor;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Version of {@link BlockApiCache} that allows BEs to opt into strong caching.
 */
// TODO: consider PR'ing this to fabric
public class MIBlockApiCache<A, C> {
    public static <A, C> MIBlockApiCache<A, C> create(BlockApiLookup<A, C> lookup, ServerWorld world, BlockPos pos) {
        return new MIBlockApiCache<>(lookup, world, pos);
    }

    private final BlockApiLookup<A, C> lookup;
    private final BlockApiCache<A, C> cache;
    @Nullable
    private A cachedApi = null;

    private final Runnable invalidateCallback = () -> cachedApi = null;

    private MIBlockApiCache(BlockApiLookup<A, C> lookup, ServerWorld world, BlockPos pos) {
        this.lookup = lookup;
        this.cache = BlockApiCache.create(lookup, world, pos);
    }

    @Nullable
    public A find(C context) {
        if (cachedApi != null) {
            return cachedApi;
        }
        A foundApi = cache.find(context);
        if (foundApi != null) {
            BlockEntity be = ((BlockApiCacheAccessor) cache).getCachedBlockEntity();
            if (be instanceof ICacheableApiHost cacheHost) {
                if (cacheHost.canCache(lookup, foundApi, invalidateCallback)) {
                    cachedApi = foundApi;
                }
            }
        }
        return foundApi;
    }
}
