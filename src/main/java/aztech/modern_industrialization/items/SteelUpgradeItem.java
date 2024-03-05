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
package aztech.modern_industrialization.items;

import aztech.modern_industrialization.MIAdvancementTriggers;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.BoilerMachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.SteamCraftingMachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.SteamWaterPumpBlockEntity;
import aztech.modern_industrialization.machines.init.MachineTier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SteelUpgradeItem extends Item {
    public SteelUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var be = context.getLevel().getBlockEntity(context.getClickedPos());
        if (!(be instanceof MachineBlockEntity machineBe)) {
            return super.useOn(context);
        }

        // Check if upgrade is allowed
        boolean canUpgrade = be instanceof SteamCraftingMachineBlockEntity steamBe && steamBe.tier == MachineTier.BRONZE
                || be instanceof BoilerMachineBlockEntity boilerBe && boilerBe.bronze
                || be instanceof SteamWaterPumpBlockEntity pumpBe && pumpBe.bronze;
        if (!canUpgrade) {
            return super.useOn(context);
        }

        // Find new block
        Block block = context.getLevel().getBlockState(context.getClickedPos()).getBlock();
        ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(block);
        ResourceLocation newBlock = blockKey.withPath(p -> p.replace("bronze_", "steel_"));
        if (!BuiltInRegistries.BLOCK.containsKey(newBlock)) {
            return super.useOn(context);
        }

        BlockState newState = BuiltInRegistries.BLOCK.get(newBlock).defaultBlockState();

        // Do the upgrade
        if (context.getLevel().isClientSide()) {
            context.getLevel().setBlockAndUpdate(context.getClickedPos(), newState);
        } else {
            CompoundTag bronzeData = machineBe.saveWithoutMetadata();
            // Remove BE to prevent duplicated items from dropping
            context.getLevel().removeBlockEntity(context.getClickedPos());
            // Update to new block
            context.getLevel().setBlockAndUpdate(context.getClickedPos(), newState);
            // Load the new data into the BE
            var newBe = context.getLevel().getBlockEntity(context.getClickedPos());
            if (!(newBe instanceof MachineBlockEntity newMachine)) {
                throw new RuntimeException("Upgraded machine should be a MachineBlockEntity, found " + newBe);
            }

            newMachine.load(bronzeData, true);

            // Give out the advancement
            if (context.getPlayer() instanceof ServerPlayer sp) {
                MIAdvancementTriggers.USED_STEEL_UPGRADE.get().trigger(sp);
            }
        }
        var pos = context.getClickedPos();
        // Play some sound as well ;)
        context.getLevel().playSound(context.getPlayer(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                newState.getSoundType().getPlaceSound(), SoundSource.BLOCKS);

        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
