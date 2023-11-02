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
package aztech.modern_industrialization.machines.guicomponents;

import aztech.modern_industrialization.MISprite;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.machines.components.RedstoneBehaviourComponent;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.machines.gui.GuiComponent;
import aztech.modern_industrialization.machines.gui.GuiComponentClient;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class ControlPanelClient implements GuiComponentClient {

    private final static Map<ResourceLocation, StatusInControlPanelClient> statusMap = new HashMap<>();

    static class StatusInControlPanelClient {

        ArrayList<MutableComponent> statusTitle = new ArrayList<>();
        ArrayList<MutableComponent> statusText = new ArrayList<>();
        ArrayList<MISprite> statusIcon = new ArrayList<>();

        public StatusInControlPanelClient withTitle(MutableComponent... title) {
            statusTitle.addAll(Arrays.asList(title));
            return this;
        }

        public StatusInControlPanelClient withTitle(MIText... title) {
            for (MIText text : title) {
                statusTitle.add(text.text());
            }
            return this;
        }

        public StatusInControlPanelClient withText(MutableComponent... text) {
            statusText.addAll(Arrays.asList(text));
            return this;
        }

        public StatusInControlPanelClient withText(MIText... text) {
            for (MIText t : text) {
                statusText.add(t.text());
            }
            return this;
        }

        public StatusInControlPanelClient withIcon(MISprite... icon) {
            statusIcon.addAll(Arrays.asList(icon));
            return this;
        }

    }

    public static StatusInControlPanelClient register(ResourceLocation id) {
        var status = new StatusInControlPanelClient();
        statusMap.put(id, status);
        return status;
    }

    static {

        register(RedstoneBehaviourComponent.ID)
                .withIcon(MISprite.REDSTONE_COMPONENT_ALWAYS_ACTIVE,
                        MISprite.REDSTONE_COMPONENT_ACTIVE_ON_HIGH_SIGNAL,
                        MISprite.REDSTONE_COMPONENT_ACTIVE_ON_LOW_SIGNAL,
                        MISprite.REDSTONE_COMPONENT_NEVER_ACTIVE)
                .withTitle(MIText.RedstoneComponentAlwaysActive,
                        MIText.RedstoneComponentActiveOnHighSignal,
                        MIText.RedstoneComponentActiveOnLowSignal,
                        MIText.RedstoneComponentNeverActive)
                .withText(MIText.RedstoneComponentAlwaysActiveText,
                        MIText.RedstoneComponentActiveOnHighSignalText,
                        MIText.RedstoneComponentActiveOnLowSignalText,
                        MIText.RedstoneComponentNeverActiveText);

    }

    private final ArrayList<StatusInControlPanelClient> buttonsData;
    private final ArrayList<Integer> buttonsState;
    private final int numberOfComponents;

    public ControlPanelClient(FriendlyByteBuf buf) {
        numberOfComponents = buf.readVarInt();
        buttonsData = new ArrayList<>(numberOfComponents);
        buttonsState = new ArrayList<>(numberOfComponents);
        for (int i = 0; i < numberOfComponents; i++) {
            buttonsData.add(statusMap.get(buf.readResourceLocation()));
        }
        readCurrentData(buf);
    }

    @Override
    public void readCurrentData(FriendlyByteBuf buf) {
        for (int i = 0; i < numberOfComponents; i++) {
            buttonsState.add(buf.readInt());
        }
    }

    @Override
    public ClientComponentRenderer createRenderer(MachineScreen machineScreen) {
        return null;
    }

    @Override
    public void setupMenu(GuiComponent.MenuFacade menu) {
        GuiComponentClient.super.setupMenu(menu);
    }

}
