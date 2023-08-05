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
package aztech.modern_industrialization.compat.waila.server;

import aztech.modern_industrialization.blocks.storage.AbstractStorageBlockEntity;
import com.google.common.primitives.Ints;
import mcp.mobius.waila.api.IDataProvider;
import mcp.mobius.waila.api.IDataWriter;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.data.ItemData;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

@SuppressWarnings("rawtypes")
public class StorageItemProvider implements IDataProvider<AbstractStorageBlockEntity> {
    @Override
    public void appendData(IDataWriter data, IServerAccessor<AbstractStorageBlockEntity> accessor, IPluginConfig config) {
        data.add(ItemData.class, res -> {
            var storage = accessor.getTarget();

            if (storage.getResource() instanceof ItemVariant variant) {
                res.add(ItemData.of(config).add(variant.toStack(Ints.saturatedCast(storage.getAmount()))));
            }

            res.block();
        });
    }
}
