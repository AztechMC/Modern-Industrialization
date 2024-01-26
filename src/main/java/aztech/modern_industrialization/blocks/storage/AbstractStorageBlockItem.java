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
package aztech.modern_industrialization.blocks.storage;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.items.ContainerItem;
import aztech.modern_industrialization.proxy.CommonProxy;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public abstract class AbstractStorageBlockItem<T extends TransferVariant<?>> extends BlockItem implements ContainerItem<T> {

    public final StorageBehaviour<T> behaviour;

    public AbstractStorageBlockItem(AbstractStorageBlock<T> block, Properties properties) {
        super(block, properties);
        this.behaviour = block.behavior;
    }

    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag context) {
        if (!isUnlocked(stack)) {
            tooltip.add(MIText.Locked.text());
        }

    }

    public StorageBehaviour<T> getBehaviour() {
        return behaviour;
    }

    // A bit stupid but I don't want to have client-only code in the main sourceset
    @Override
    @SuppressWarnings("rawtype")
    public void initializeClient(Consumer stupidClientProperties) {
        CommonProxy.INSTANCE.withStandardItemRenderer(stupidClientProperties);
    }
}
