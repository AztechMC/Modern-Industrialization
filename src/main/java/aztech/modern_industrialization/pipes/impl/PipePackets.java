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

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.pipes.item.ItemPipeScreenHandler;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class PipePackets {
    public static final Identifier SET_ITEM_WHITELIST = new MIIdentifier("set_item_whitelist");
    public static final PacketConsumer ON_SET_ITEM_WHITELIST = (context, data) -> {
        int syncId = data.readInt();
        boolean whitelist = data.readBoolean();
        context.getTaskQueue().execute(() -> {
            ScreenHandler handler = context.getPlayer().currentScreenHandler;
            if (handler.syncId == syncId) {
                ((ItemPipeScreenHandler) handler).pipeInterface.setWhitelist(whitelist);
            }
        });
    };
    public static final Identifier SET_ITEM_CONNECTION_TYPE = new MIIdentifier("set_item_connection_type");
    public static final PacketConsumer ON_SET_ITEM_CONNECTION_TYPE = (context, data) -> {
        int syncId = data.readInt();
        int type = data.readInt();
        context.getTaskQueue().execute(() -> {
            ScreenHandler handler = context.getPlayer().currentScreenHandler;
            if (handler.syncId == syncId) {
                ((ItemPipeScreenHandler) handler).pipeInterface.setConnectionType(type);
            }
        });
    };
    public static final Identifier INCREMENT_ITEM_PRIORITY = new MIIdentifier("increment_item_priority");
    public static final PacketConsumer ON_INCREMENT_ITEM_PRIORITY = (context, data) -> {
        int syncId = data.readInt();
        int priority = data.readInt();
        context.getTaskQueue().execute(() -> {
            ScreenHandler handler = context.getPlayer().currentScreenHandler;
            if (handler.syncId == syncId) {
                ((ItemPipeScreenHandler) handler).pipeInterface.incrementPriority(priority);
            }
        });
    };
    public static final Identifier SET_ITEM_PRIORITY = new MIIdentifier("set_item_priority");
    public static final PacketConsumer ON_SET_ITEM_PRIORITY = (context, data) -> {
        int syncId = data.readInt();
        int priority = data.readInt();
        context.getTaskQueue().execute(() -> {
            ScreenHandler handler = context.getPlayer().currentScreenHandler;
            if (handler.syncId == syncId) {
                ((ItemPipeScreenHandler) handler).pipeInterface.setPriority(priority);
            }
        });
    };
}
