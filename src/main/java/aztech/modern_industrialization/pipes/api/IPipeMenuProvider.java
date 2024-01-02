package aztech.modern_industrialization.pipes.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.MenuProvider;

public interface IPipeMenuProvider extends MenuProvider {
    void writeAdditionalData(FriendlyByteBuf buf);
}
