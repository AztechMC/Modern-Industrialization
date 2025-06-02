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
package aztech.modern_industrialization.machines.multiblocks;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIText;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public enum HatchType {
    ITEM_INPUT(0, MIText.ItemInputHatch),
    ITEM_OUTPUT(1, MIText.ItemOutputHatch),
    FLUID_INPUT(2, MIText.FluidInputHatch),
    FLUID_OUTPUT(3, MIText.FluidOutputHatch),
    ENERGY_INPUT(4, MIText.EnergyInputHatch),
    ENERGY_OUTPUT(5, MIText.EnergyOutputHatch),
    NUCLEAR_ITEM(6, "nuclear_item_hatch"),
    NUCLEAR_FLUID(7, "nuclear_fluid_hatch"),
    LARGE_TANK(8, "large_tank_hatch");

    private final int id;
    private final Either<MIText, ResourceLocation> descriptionOrBlockId;

    HatchType(int id, MIText description) {
        this.id = id;
        this.descriptionOrBlockId = Either.left(description);
    }

    HatchType(int id, String blockId) {
        this.id = id;
        this.descriptionOrBlockId = Either.right(MI.id(blockId));
    }

    public int getId() {
        return id;
    }

    public MutableComponent description() {
        return descriptionOrBlockId.map(MIText::text, id -> BuiltInRegistries.BLOCK.get(id).getName());
    }
}
