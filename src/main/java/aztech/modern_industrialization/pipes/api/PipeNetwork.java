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
package aztech.modern_industrialization.pipes.api;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A pipe network. It is very important that you create a new empty data object
 * if your constructor was passed null.
 */
public abstract class PipeNetwork {
    protected int id;
    public PipeNetworkManager manager;
    public PipeNetworkData data;
    public Map<BlockPos, PipeNetworkNode> nodes = new HashMap<>();
    public boolean ticked = false;

    public PipeNetwork(int id, PipeNetworkData data) {
        this.id = id;
        this.data = data;
    }

    public void fromTag(NbtCompound tag) {
        id = tag.getInt("id");
        data.fromTag(tag.getCompound("data"));
    }

    public NbtCompound toTag(NbtCompound tag) {
        tag.putInt("id", id);
        tag.put("data", data.toTag(new NbtCompound()));
        return tag;
    }

    public void tick(World world) {

    }

    /**
     * Allow merging networks when the player explicitly requests to do so. When
     * this function is called, it must return a new PipeNetworkData without
     * modifying either itself or its parameter.
     * 
     * @return null if there can be no merge, or the new pipe network data should
     *         there be a merge.
     */
    public PipeNetworkData merge(PipeNetwork other) {
        return null;
    }
}
