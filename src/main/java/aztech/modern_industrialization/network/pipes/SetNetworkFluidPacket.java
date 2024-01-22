package aztech.modern_industrialization.network.pipes;

import aztech.modern_industrialization.network.BasePacket;
import aztech.modern_industrialization.pipes.fluid.FluidPipeScreenHandler;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record SetNetworkFluidPacket(int syncId, FluidVariant fluid) implements BasePacket {
    public SetNetworkFluidPacket(FriendlyByteBuf buf) {
        this(buf.readUnsignedByte(), FluidVariant.fromPacket(buf));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(syncId);
        fluid.toPacket(buf);
    }

    @Override
    public void handle(Context ctx) {
        AbstractContainerMenu handler = ctx.getPlayer().containerMenu;
        if (handler.containerId == syncId) {
            ((FluidPipeScreenHandler) handler).iface.setNetworkFluid(fluid);
        }
    }
}
