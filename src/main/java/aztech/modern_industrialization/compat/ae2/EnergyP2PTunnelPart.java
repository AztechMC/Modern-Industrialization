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
package aztech.modern_industrialization.compat.ae2;

import appeng.api.config.PowerUnits;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.parts.p2p.CapabilityP2PTunnelPart;
import aztech.modern_industrialization.api.energy.*;
import aztech.modern_industrialization.util.Simulation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.Util;

public class EnergyP2PTunnelPart extends CapabilityP2PTunnelPart<EnergyP2PTunnelPart, EnergyMoveable> {

    private static final P2PModels MODELS = new P2PModels("part/energy_p2p_tunnel");

    public EnergyP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem, EnergyApi.MOVEABLE);

        inputHandler = new InputEnergyStorage();
        outputHandler = new OutputEnergyStorage();
        emptyHandler = new EnergyMoveable() {
        };
    }

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    private class InputEnergyStorage implements EnergyInsertable {
        @Override
        public long insertEnergy(long amount, Simulation simulation) {
            StoragePreconditions.notNegative(amount);

            var total = 0L;
            var outputTunnels = getOutputs().size();

            if (outputTunnels == 0 || amount == 0) {
                return 0;
            }

            var amountPerOutput = amount / outputTunnels;
            var overflow = amountPerOutput == 0 ? amount : amount % amountPerOutput;

            for (var target : getOutputs()) {
                try (var capabilityGuard = target.getAdjacentCapability()) {
                    if (get(capabilityGuard) instanceof EnergyInsertable insertable) {
                        var toSend = amountPerOutput + overflow;
                        var received = insertable.insertEnergy(toSend, simulation);

                        overflow = toSend - received;
                        total += received;
                    }
                }
            }

            try (var tx = Transaction.openOuter()) {
                queueTunnelDrain(PowerUnits.TR, total, tx);

                if (simulation.isActing()) {
                    tx.commit();
                }
            }

            return total;
        }

        @Override
        public boolean canInsert(CableTier tier) {
            return tier == CableTier.SUPERCONDUCTOR;
        }
    }

    private class OutputEnergyStorage implements EnergyExtractable {
        @Override
        public long extractEnergy(long maxAmount, Simulation simulation) {
            try (var input = getInputCapability()) {
                if (get(input) instanceof EnergyExtractable extractable) {
                    var extracted = extractable.extractEnergy(maxAmount, simulation);

                    try (var tx = Transaction.openOuter()) {
                        queueTunnelDrain(PowerUnits.TR, extracted, tx);

                        if (simulation.isActing()) {
                            tx.commit();
                        }
                    }

                    return extracted;
                }
            }

            return 0;
        }

        @Override
        public boolean canExtract(CableTier tier) {
            return tier == CableTier.SUPERCONDUCTOR;
        }
    }

    private static final MethodHandle GET = Util.make(() -> {
        try {
            var get = CapabilityGuard.class.getDeclaredMethod("get");
            get.setAccessible(true);
            return MethodHandles.lookup().unreflect(get);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    });

    private EnergyMoveable get(CapabilityGuard guard) {
        try {
            return (EnergyMoveable) (Object) GET.invokeExact(guard);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
