package aztech.modern_industrialization.machinesv2.gui;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Renderer for the shared data of a client component on the machine screen.
 */
public interface ClientComponentRenderer {
    void renderBackground(DrawableHelper helper, MatrixStack matrices, int x, int y);
    default void renderTooltip(DrawableHelper helper, MatrixStack matrices, int x, int y, int cursorX, int cursorY) {
    }
}
