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
import net.minecraft.resources.Identifier;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

/**
 * Provider for the stored fluid of a tank.
 * Mainly exists because of the quantum tank which stores more fluid than can be queried through IFluidHandler.
 */
public class TankComponentProvider implements IServerExtensionProvider<FluidView.Data>, IClientExtensionProvider<FluidView.Data, FluidView> {
    @Override
    public List<ViewGroup<FluidView.Data>> getGroups(Accessor<?> accessor) {
        var tank = (TankBlockEntity) accessor.getTarget();
        var resource = tank.getResource(0);
        return List.of(new ViewGroup<>(List.of(
                new FluidView.Data(MIJadeCommonPlugin.fluidStack(resource, tank.getAmountAsLong(0)), tank.getCapacityAsLong(0, resource)))));
    }

    @Override
    public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<FluidView.Data>> list) {
        return ClientViewGroup.map(list, FluidView::readDefault, null);
    }

    @Override
    public Identifier getUid() {
        return MI.id("tank");
    }
}
