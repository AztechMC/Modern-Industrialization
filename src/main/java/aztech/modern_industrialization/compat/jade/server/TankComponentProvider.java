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
package aztech.modern_industrialization.compat.jade.server;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.blocks.storage.tank.TankBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

/**
 * Provider for the stored fluid of a tank.
 * Mainly exists because of the quantum tank which stores more fluid than can be queried through IFluidHandler.
 */
public class TankComponentProvider implements IServerExtensionProvider<CompoundTag>, IClientExtensionProvider<CompoundTag, FluidView> {
    @Override
    public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor) {
        var tank = (TankBlockEntity) accessor.getTarget();
        return List.of(new ViewGroup<>(List.of(
                FluidView.writeDefault(MIJadeCommonPlugin.fluidStack(tank.getResource(), tank.getAmount()), tank.getCapacity()))));
    }

    @Override
    public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> list) {
        return ClientViewGroup.map(list, TankComponentProvider::readFluidViewLong, null);
    }

    /**
     * Copy/paste of {@link FluidView#readDefault(CompoundTag)} with fluid name getting changed such that it stops showing Air for large amounts.
     */
    @Nullable
    public static FluidView readFluidViewLong(CompoundTag tag) {
        long capacity = tag.getLong("capacity");
        if (capacity <= 0L) {
            return null;
        }
        JadeFluidObject fluidObject = JadeFluidObject.CODEC.parse(NbtOps.INSTANCE, tag.get("fluid")).result().orElse(null);
        if (fluidObject == null) {
            return null;
        }
        long amount = fluidObject.getAmount();
        FluidView fluidView = new FluidView(IElementHelper.get().fluid(fluidObject));
        fluidView.fluidName = new FluidStack(fluidObject.getType().builtInRegistryHolder(), 1, fluidObject.getComponents()).getHoverName();
        fluidView.current = IDisplayHelper.get().humanReadableNumber((double) amount, "B", true);
        fluidView.max = IDisplayHelper.get().humanReadableNumber((double) capacity, "B", true);
        fluidView.ratio = (float) ((double) amount / (double) capacity);
        if (fluidObject.getType().isSame(Fluids.EMPTY)) {
            fluidView.overrideText = Component.translatable("jade.fluid", FluidView.EMPTY_FLUID,
                    Component.literal(fluidView.max).withStyle(ChatFormatting.GRAY));
        }

        return fluidView;
    }

    @Override
    public ResourceLocation getUid() {
        return MI.id("tank");
    }
}
