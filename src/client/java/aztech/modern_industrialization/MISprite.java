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
package aztech.modern_industrialization;

import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.machines.guicomponents.ControlPanelClient;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

public enum MISprite {

    REDSTONE_COMPONENT_ALWAYS_ACTIVE(0, 0, 18, 18),
    REDSTONE_COMPONENT_ACTIVE_ON_HIGH_SIGNAL(18, 0, 18, 18),
    REDSTONE_COMPONENT_ACTIVE_ON_LOW_SIGNAL(36, 0, 18, 18),
    REDSTONE_COMPONENT_NEVER_ACTIVE(54, 0, 18, 18);

    MISprite(int u, int v, int width, int height) {
        this(u, v, width, height, MachineScreen.SLOT_ATLAS);
    }

    MISprite(int u, int v, int width, int height, ResourceLocation textureAtlas) {
        this(u, v, width, height, textureAtlas, null);
    }

    MISprite(int u, int v, int width, int height, ResourceLocation textureAtlas, Enum<?> buttonState) {
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
        this.textureAtlas = textureAtlas;
        if (buttonState != null) {
            ControlPanelClient.registerButtonSprite(buttonState, this);
        }
    }

    MISprite(int u, int v, int width, int height, Enum<?> buttonState) {
        this(u, v, width, height, MachineScreen.SLOT_ATLAS, buttonState);
    }

    public void draw(int x, int y, Screen screen, PoseStack poseStack) {
        RenderSystem.setShaderTexture(0, textureAtlas);
        screen.blit(poseStack, x, y, u, v, width, height);
    }

    private final int u, v, width, height;
    private final ResourceLocation textureAtlas;
}
