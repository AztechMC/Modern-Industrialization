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

import aztech.modern_industrialization.machines.GuiComponents;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.machines.gui.GuiComponent;
import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ControlPanel {

    /*
     * A server class which is used to store a list of button linked to a internal state of the machine stored in a Component which stores a finite
     * number of state in an integer.
     */

    public static class Server implements GuiComponent.ServerIntegerList {

        ArrayList<StatusInControlPanel> components = new ArrayList<>();

        @Override
        public ResourceLocation getId() {
            return GuiComponents.CONTROL_PANEL;
        }

        public Server(StatusInControlPanel... components) {
            this.components.addAll(Arrays.asList(components));
        }

        @Override
        public int getIntegerAtIndex(int index) {
            return components.get(index).stateValue();
        }

        @Override
        public int getIntegerListSize() {
            return components.size();
        }

        @Override
        public void writeAdditionalInitialData(FriendlyByteBuf buf) {
            for (StatusInControlPanel component : components) {
                buf.writeResourceLocation(component.getId());
            }
        }

        public void onClickOnButton(int buttonId, boolean clickedLeftButton) {
            StatusInControlPanel component = components.get(buttonId);
            int currentState = component.stateValue();
            int numberOfStates = component.numberOfStates();
            component.setStateValue(clickedLeftButton ? (currentState + 1) % numberOfStates : (currentState - 1 + numberOfStates) % numberOfStates);

        }
    }

    public interface StatusInControlPanel extends IComponent.ServerOnly {

        // NBT String key to store the state of the component in the machine
        default String nbtString() {
            return "status";
        }

        // Default state of the component if the machine is placed for the first time / the nbt is corrupted
        default int defaultState() {
            return 0;
        }

        // The id of the component in the control panel, is used to link to client component
        ResourceLocation getId();

        void setStateValue(int value);

        int stateValue();

        int numberOfStates();

        default void writeNbt(CompoundTag tag) {
            tag.putInt(nbtString(), stateValue());
        }

        default void readNbt(CompoundTag tag) {
            if (tag.contains(nbtString())) {
                this.setStateValue(tag.getInt(nbtString()));
            } else {
                setStateValue(defaultState());
            }
        }
    }

}
