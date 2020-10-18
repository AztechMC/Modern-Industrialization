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
package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.pipes.api.PipeNetworkManager;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.api.PipeNetworksComponent;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class PipeNetworksComponentImpl implements PipeNetworksComponent {
    private Map<PipeNetworkType, PipeNetworkManager> managers = new HashMap<>();

    public PipeNetworksComponentImpl(World world) {
        for (PipeNetworkType type : PipeNetworkType.getTypes().values()) {
            managers.put(type, new PipeNetworkManager(type));
        }
    }

    @Override
    public PipeNetworkManager getManager(PipeNetworkType type) {
        return managers.get(type);
    }

    @Override
    public void onServerTickStart() {
        for (PipeNetworkManager manager : managers.values()) {
            manager.markNetworksAsUnticked();
        }
    }

    @Override
    public void fromTag(CompoundTag tag) {
        for (Map.Entry<Identifier, PipeNetworkType> entry : PipeNetworkType.getTypes().entrySet()) {
            PipeNetworkManager manager = new PipeNetworkManager(entry.getValue());
            String tagKey = entry.getKey().toString();
            if (tag.contains(tagKey)) {
                manager.fromTag(tag.getCompound(tagKey));
            }
            managers.put(entry.getValue(), manager);
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        for (Map.Entry<PipeNetworkType, PipeNetworkManager> entry : managers.entrySet()) {
            tag.put(entry.getKey().getIdentifier().toString(), entry.getValue().toTag(new CompoundTag()));
        }
        return tag;
    }
}
