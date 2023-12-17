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
package aztech.modern_industrialization.inventory;

import aztech.modern_industrialization.MIIdentifier;
import net.minecraft.resources.ResourceLocation;

public class ConfigurableInventoryPackets {
    public static final ResourceLocation UPDATE_ITEM_SLOT = new MIIdentifier("update_item_slot");
    public static final ResourceLocation UPDATE_FLUID_SLOT = new MIIdentifier("update_fluid_slot");
    public static final ResourceLocation LOCK_ALL = new MIIdentifier("lock_all");
    public static final ResourceLocation SET_LOCKING_MODE = new MIIdentifier("set_locking_mode");
    public static final ResourceLocation DO_SLOT_DRAGGING = new MIIdentifier("do_slot_dragging");
    public static final ResourceLocation ADJUST_SLOT_CAPACITY = new MIIdentifier("adjust_slot_capacity");
}
