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
package aztech.modern_industrialization.machinesv2.init;

import static aztech.modern_industrialization.machines.impl.multiblock.HatchType.*;

import aztech.modern_industrialization.machinesv2.blockentities.multiblocks.SteamCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machinesv2.models.MachineCasings;
import aztech.modern_industrialization.machinesv2.models.MachineModels;
import aztech.modern_industrialization.machinesv2.multiblocks.HatchFlags;
import aztech.modern_industrialization.machinesv2.multiblocks.MultiblockMachineBER;
import aztech.modern_industrialization.machinesv2.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machinesv2.multiblocks.SimpleMember;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;

@SuppressWarnings("rawtypes")
public class MultiblockMachines {
    public static BlockEntityType COKE_OVEN;

    public static void init() {
        SimpleMember bricks = SimpleMember.forBlock(Blocks.BRICKS);
        HatchFlags steamCraftingHatches = new HatchFlags.Builder().with(ITEM_INPUT).with(ITEM_OUTPUT).with(FLUID_INPUT).build();
        ShapeTemplate cokeOvenShape = new ShapeTemplate.Builder(MachineCasings.BRICKS).add3by3(-1, bricks, false, steamCraftingHatches)
                .add3by3(0, bricks, true, null).add3by3(1, bricks, true, null).build();
        COKE_OVEN = MachineRegistrationHelper.registerMachine("coke_oven",
                bet -> new SteamCraftingMultiblockBlockEntity(bet, "coke_oven", cokeOvenShape, MIMachineRecipeTypes.COKE_OVEN));
    }

    @SuppressWarnings("unchecked")
    public static void clientInit() {
        MachineModels.addTieredMachine("coke_oven", "coke_oven", MachineCasings.BRICKS, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(COKE_OVEN, MultiblockMachineBER::new);
    }
}
