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
package aztech.modern_industrialization.compat.waila.client.component;

import static net.minecraft.client.gui.GuiComponent.fill;

import aztech.modern_industrialization.MIIdentifier;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import mcp.mobius.waila.api.ITooltipComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * This is a modified version of Megane code, as it is not yet available in the WAILA API.
 *
 * Copyright (c) 2020-2022 deirn
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
public class BarComponent implements ITooltipComponent {

    private static final ResourceLocation TEXTURE = new MIIdentifier("textures/gui/megane_bar.png");

    private final int color;
    private final double stored, max;

    private final String valString;

    public BarComponent(int color, double stored, double max, String unit, boolean verbose, boolean castToInt) {
        this.color = color;
        this.stored = stored;
        this.max = max;

        String storedString;
        if (stored < 0 || stored == Double.MAX_VALUE) {
            storedString = "∞";
        } else {
            if (castToInt) {
                storedString = verbose ? String.valueOf((int) stored) : MeganeUtils.suffix((long) stored);
            } else {
                storedString = verbose ? String.valueOf(stored) : MeganeUtils.suffix((long) stored);
            }
        }

        String maxString;
        if (max <= 0 || max == Double.MAX_VALUE) {
            maxString = "∞";
        } else {
            if (castToInt) {
                maxString = verbose ? String.valueOf((int) max) : MeganeUtils.suffix((long) max);
            } else {
                maxString = verbose ? String.valueOf(max) : MeganeUtils.suffix((long) max);
            }
        }

        valString = storedString + "/" + maxString + (unit.isEmpty() ? "" : " " + unit);
    }

    public BarComponent(int color, double stored, double max, String unit, boolean verbose) {
        this(color, stored, max, unit, verbose, false);
    }

    @Override
    public int getWidth() {
        return Math.max(MeganeUtils.textRenderer().width(valString), 100);
    }

    @Override
    public int getHeight() {
        return 13;
    }

    @Override
    public void render(PoseStack matrices, int x, int y, float delta) {
        float ratio = max == 0 ? 1F : ((float) Math.floor((Math.min((float) (stored / max), 1F)) * 100)) / 100F;

        MeganeUtils.drawTexture(matrices, TEXTURE, x, y, 100, 11, 0, 0, 1F, 0.5F, color);
        MeganeUtils.drawTexture(matrices, TEXTURE, x, y, (int) (ratio * 100), 11, 0, 0.5F, ratio, 1F, color);

        double brightness = MeganeUtils.getBrightness(color);
        int overlay = 0;

        if (brightness < 0.25)
            overlay = 0x08FFFFFF;
        else if (brightness > 0.90)
            overlay = 0x80000000;
        else if (brightness > 0.80)
            overlay = 0x70000000;
        else if (brightness > 0.70)
            overlay = 0x60000000;
        else if (brightness > 0.60)
            overlay = 0x50000000;
        else if (brightness > 0.50)
            overlay = 0x40000000;

        fill(matrices, x, y, x + 100, y + 11, overlay);

        int textWidth = MeganeUtils.textRenderer().width(valString);
        float textX = x + Math.max((100 - textWidth) / 2F, 0F);
        float textY = y + 2;
        MeganeUtils.textRenderer().draw(matrices, valString, textX, textY, 0xFFAAAAAA);
    }

    public static final class MeganeUtils {

        private static final NavigableMap<Long, String> SUFFIXES = new TreeMap<>();

        static {
            MeganeUtils.SUFFIXES.put(1000L, "K");
            MeganeUtils.SUFFIXES.put(1000000L, "M");
            MeganeUtils.SUFFIXES.put(1000000000L, "G");
            MeganeUtils.SUFFIXES.put(1000000000000L, "T");
            MeganeUtils.SUFFIXES.put(1000000000000000L, "P");
            MeganeUtils.SUFFIXES.put(1000000000000000000L, "E");
        }

        @Environment(EnvType.CLIENT)
        public static void drawTexture(
                PoseStack matrices, ResourceLocation id,
                int x, int y, int w, int h,
                float u0, float v0, float u1, float v1, int color) {
            matrices.pushPose();

            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
            RenderSystem.setShaderTexture(0, id);

            int a = 0xFF;
            int r = getR(color);
            int g = getG(color);
            int b = getB(color);

            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

            buffer.vertex(matrices.last().pose(), x, y + h, 0).color(r, g, b, a).uv(u0, v1).endVertex();
            buffer.vertex(matrices.last().pose(), x + w, y + h, 0).color(r, g, b, a).uv(u1, v1).endVertex();
            buffer.vertex(matrices.last().pose(), x + w, y, 0).color(r, g, b, a).uv(u1, v0).endVertex();
            buffer.vertex(matrices.last().pose(), x, y, 0).color(r, g, b, a).uv(u0, v0).endVertex();

            tessellator.end();
            RenderSystem.disableBlend();
            matrices.popPose();
        }

        public static int getR(int aarrggbb) {
            return (aarrggbb >> 16) & 0xFF;
        }

        public static int getG(int aarrggbb) {
            return (aarrggbb >> 8) & 0xFF;
        }

        public static int getB(int aarrggbb) {
            return aarrggbb & 0xFF;
        }

        public static double getBrightness(int color) {
            return (0.299 * getR(color) + 0.587 * getG(color) + 0.114 * getB(color)) / 255.0;
        }

        @SuppressWarnings("IntegerDivisionInFloatingPointContext")
        public static String suffix(long value) {
            if (value == Long.MIN_VALUE)
                return suffix(Long.MIN_VALUE + 1);
            if (value < 0)
                return "-" + suffix(-value);
            if (value < 1000)
                return Long.toString(value);

            Map.Entry<Long, String> e = SUFFIXES.floorEntry(value);
            Long divideBy = e.getKey();
            String suffix = e.getValue();
            long truncated = value / (divideBy / 10);
            boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
            return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
        }

        @Environment(EnvType.CLIENT)
        public static Font textRenderer() {
            return Minecraft.getInstance().font;
        }

    }
}
