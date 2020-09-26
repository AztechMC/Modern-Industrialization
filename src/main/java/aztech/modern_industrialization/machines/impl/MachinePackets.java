package aztech.modern_industrialization.machines.impl;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class MachinePackets {
    public static class S2C {
        public static final Identifier UPDATE_AUTO_EXTRACT = new MIIdentifier("update_auto_extract");
        public static final PacketConsumer ON_UPDATE_AUTO_EXTRACT = (context, data) -> {
            int syncId = data.readInt();
            boolean itemExtract = data.readBoolean();
            boolean fluidExtract = data.readBoolean();
            context.getTaskQueue().execute(() -> {
                ScreenHandler handler = context.getPlayer().currentScreenHandler;
                if(handler.syncId == syncId) {
                    ((MachineScreenHandler) handler).inventory.setItemExtract(itemExtract);
                    ((MachineScreenHandler) handler).inventory.setFluidExtract(fluidExtract);
                }
            });
        };
    }
    public static class C2S {
        public static final Identifier SET_AUTO_EXTRACT = new MIIdentifier("set_auto_extract");
        public static final PacketConsumer ON_SET_AUTO_EXTRACT = (context, data) -> {
            int syncId = data.readInt();
            boolean isItem = data.readBoolean();
            boolean isExtract = data.readBoolean();
            context.getTaskQueue().execute(() -> {
                ScreenHandler handler = context.getPlayer().currentScreenHandler;
                if(handler.syncId == syncId) {
                    if(isItem) {
                        ((MachineScreenHandler) handler).inventory.setItemExtract(isExtract);
                    } else {
                        ((MachineScreenHandler) handler).inventory.setFluidExtract(isExtract);
                    }
                }
            });
        };
        public static final Identifier LOCK_RECIPE = new MIIdentifier("lock_recipe");
        public static final PacketConsumer ON_LOCK_RECIPE = (context, data) -> {
            int syncId = data.readInt();
            Identifier recipeId = new Identifier(data.readString());
            context.getTaskQueue().execute(() -> {
                ScreenHandler handler = context.getPlayer().currentScreenHandler;
                if(handler.syncId == syncId && handler instanceof MachineScreenHandler) {
                    Recipe recipe = context.getPlayer().world.getRecipeManager().get(recipeId).orElse(null);
                    if(recipe instanceof MachineRecipe) {
                        MachineScreenHandler machineHandler = (MachineScreenHandler) handler;
                        // this cast should always be safe because we are on the logical server
                        MachineBlockEntity be = (MachineBlockEntity) machineHandler.inventory;
                        be.lockRecipe((MachineRecipe) recipe, context.getPlayer().inventory);
                    }
                }
            });
        };
    }
}
