package aztech.modern_industrialization.machinesv2.gui;

import aztech.modern_industrialization.machinesv2.MachineScreenHandlers;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import javax.tools.Tool;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Renderer for the shared data of a client component on the machine screen.
 */
public interface ClientComponentRenderer {
    default void addButtons(ButtonContainer container) {
    }
    void renderBackground(DrawableHelper helper, MatrixStack matrices, int x, int y);
    default void renderTooltip(MachineScreenHandlers.ClientScreen screen, MatrixStack matrices, int x, int y, int cursorX, int cursorY) {
    }

    interface ButtonContainer {
        /**
         * @param u Texture button u.
         * @param message No idea.
         * @param pressAction A function that is called with the syncId every time the button is pressed.
         * @param tooltipSupplier A function that is called every time the tooltip must be rendered.
         * @param isPressed A function that returns true if the button is currently pressed.
         */
        void addButton(int u, Text message, Consumer<Integer> pressAction, Supplier<List<Text>> tooltipSupplier, Supplier<Boolean> isPressed);
    }
}
