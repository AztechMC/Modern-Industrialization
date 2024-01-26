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
package aztech.modern_industrialization.machines.models;

import java.util.Objects;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

public class MachineModelClientData {
    public static final ModelProperty<MachineModelClientData> KEY = new ModelProperty<>();

    /**
     * May be null to use the default casing.
     */
    @Nullable
    public final MachineCasing casing;
    public Direction frontDirection;
    public boolean isActive = false;
    /**
     * May be null for no output.
     */
    public Direction outputDirection = null;
    public boolean itemAutoExtract = false;
    public boolean fluidAutoExtract = false;

    public MachineModelClientData() {
        this(null);
    }

    public MachineModelClientData(@Nullable MachineCasing casing) {
        this.casing = casing;
    }

    public MachineModelClientData(@Nullable MachineCasing casing, Direction frontDirection) {
        this.casing = casing;
        this.frontDirection = frontDirection;
    }

    public MachineModelClientData active(boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MachineModelClientData that = (MachineModelClientData) o;
        return isActive == that.isActive && itemAutoExtract == that.itemAutoExtract && fluidAutoExtract == that.fluidAutoExtract
                && Objects.equals(casing, that.casing) && frontDirection == that.frontDirection && outputDirection == that.outputDirection;
    }

    @Override
    public int hashCode() {
        return Objects.hash(casing, frontDirection, isActive, outputDirection, itemAutoExtract, fluidAutoExtract);
    }
}
