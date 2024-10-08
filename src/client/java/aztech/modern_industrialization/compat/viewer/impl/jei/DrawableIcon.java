package aztech.modern_industrialization.compat.viewer.impl.jei;

import aztech.modern_industrialization.compat.viewer.abstraction.ViewerCategory;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;

public class DrawableIcon implements IDrawable {
	private final ViewerCategory.Icon.Texture texture;

	public static IDrawable create(IGuiHelper guiHelper, ViewerCategory.Icon icon) {
		return switch (icon) {
			case ViewerCategory.Icon.Stack stack -> guiHelper.createDrawableItemStack(stack.stack());
			case ViewerCategory.Icon.Texture texture -> new DrawableIcon(texture);
			case null -> throw new NullPointerException("Icon cannot be null");
		};
	}

	public DrawableIcon(ViewerCategory.Icon.Texture texture) {
		this.texture = texture;
	}

	@Override
	public int getWidth() {
		return 18;
	}

	@Override
	public int getHeight() {
		return 18;
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		guiGraphics.blit(texture.loc(), xOffset - 1, yOffset - 1, 0, texture.u(), texture.v(), 18, 18, 256, 256);
	}
}
