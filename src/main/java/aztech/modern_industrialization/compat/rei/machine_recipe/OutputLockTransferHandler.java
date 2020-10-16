package aztech.modern_industrialization.compat.rei.machine_recipe;

import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.MachinePackets;
import aztech.modern_industrialization.machines.impl.MachineScreen;
import aztech.modern_industrialization.machines.impl.MachineScreenHandler;
import io.netty.buffer.Unpooled;
import me.shedaniel.rei.api.AutoTransferHandler;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;

public class OutputLockTransferHandler implements AutoTransferHandler {
    @Override
    public @NotNull Result handle(@NotNull Context context) {
        if(!(context.getRecipe() instanceof MachineRecipeDisplay))
            return Result.createNotApplicable();
        if(!(context.getContainerScreen() instanceof MachineScreen))
            return Result.createNotApplicable();
        MachineRecipeDisplay display = (MachineRecipeDisplay) context.getRecipe();
        MachineScreen screen = (MachineScreen) context.getContainerScreen();
        MachineScreenHandler handler = screen.getScreenHandler();
        MachineFactory factory = handler.getMachineFactory();
        if(factory.recipeType != display.recipe.getType())
            return Result.createNotApplicable();
        // Try to lock output slots
        if(context.isActuallyCrafting()) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(handler.syncId);
            buf.writeIdentifier(display.recipe.getId());
            ClientSidePacketRegistry.INSTANCE.sendToServer(MachinePackets.C2S.LOCK_RECIPE, buf);
        }
        return Result.createSuccessful().blocksFurtherHandling(true);
    }
}
