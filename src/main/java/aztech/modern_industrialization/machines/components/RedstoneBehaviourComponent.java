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
package aztech.modern_industrialization.machines.components;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.guicomponents.ControlPanel;
import net.minecraft.resources.ResourceLocation;

public class RedstoneBehaviourComponent implements ControlPanel.StatusInControlPanel {

    public static final ResourceLocation ID = new MIIdentifier("redstone_behaviour");

    private RedstoneBehaviour behaviour;
    private final RedstoneStatusComponent statusComponent;

    public RedstoneBehaviourComponent(RedstoneStatusComponent statusComponent) {
        this.behaviour = RedstoneBehaviour.ALWAYS_ACTIVE;
        this.statusComponent = statusComponent;
    }

    public boolean doAllowNormalOperation() {
        return switch (behaviour) {
        case ALWAYS_ACTIVE -> true;
        case ACTIVE_ON_HIGH_SIGNAL -> statusComponent.isPowered;
        case ACTIVE_ON_LOW_SIGNAL -> !statusComponent.isPowered;
        default -> false;
        };
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void setStateValue(int value) {
        behaviour = RedstoneBehaviour.values()[value];
    }

    @Override
    public int stateValue() {
        return behaviour.ordinal();
    }

    @Override
    public int numberOfStates() {
        return RedstoneBehaviour.values().length;
    }

    public enum RedstoneBehaviour {
        ALWAYS_ACTIVE,
        ACTIVE_ON_HIGH_SIGNAL,
        ACTIVE_ON_LOW_SIGNAL,
        NEVER_ACTIVE;
    }
}
