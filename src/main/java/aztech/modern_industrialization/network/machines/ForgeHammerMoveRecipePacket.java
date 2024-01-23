package aztech.modern_industrialization.network.machines;

import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreenHandler;
import aztech.modern_industrialization.network.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record ForgeHammerMoveRecipePacket(int containedId, ResourceLocation recipeId, int fillAction, int amount) implements BasePacket {
    public ForgeHammerMoveRecipePacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readResourceLocation(), buf.readByte(), buf.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(containedId);
        buf.writeResourceLocation(recipeId);
        buf.writeByte(fillAction);
        buf.writeInt(amount);
    }

    @Override
    public void handle(Context ctx) {
        ctx.assertOnServer();

        AbstractContainerMenu menu = ctx.getPlayer().containerMenu;
        if (menu.containerId == containedId && menu instanceof ForgeHammerScreenHandler fh) {
            fh.moveRecipe(recipeId, fillAction, amount);
        }
    }
}
