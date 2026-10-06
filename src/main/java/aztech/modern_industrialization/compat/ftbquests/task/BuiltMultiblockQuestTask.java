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

package aztech.modern_industrialization.compat.ftbquests.task;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.advancement.multiblock.BuiltMultiblockContext;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import com.google.common.base.Suppliers;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.AbstractBooleanTask;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

public class BuiltMultiblockQuestTask extends AbstractBooleanTask {
    private static final Supplier<NameMap<BuiltMultiblockContext>> multiblockNameMap = Suppliers.memoize(BuiltMultiblockQuestTask::collectMultiblockTypes);

    private static List<BuiltMultiblockContext> collectMultiblockOptions() {
        List<BuiltMultiblockContext> options = new ArrayList<>();
        for (var block : BuiltInRegistries.BLOCK) {
            if (block instanceof MachineBlock machineBlock && machineBlock.getBlockEntityInstance() instanceof MultiblockMachineBlockEntity multiblock) {
                var machineId = multiblock.guiParams.blockId;
                // Always include an option for every multiblock
                options.add(new BuiltMultiblockContext(machineId, Optional.empty()));
                // Include all the registered shapes for this multiblock
                for (var multiblockShape : ReiMachineRecipes.multiblockShapes) {
                    if (multiblockShape.machine().equals(machineId) && multiblockShape.alternative() != null) {
                        options.add(new BuiltMultiblockContext(machineId, multiblockShape.alternative()));
                    }
                }
            }
        }
        return options;
    }

    private static NameMap<BuiltMultiblockContext> collectMultiblockTypes() {
        List<BuiltMultiblockContext> options = collectMultiblockOptions();
        // Sort by id, then by shape. Options with no shape associated are ordered first
        options.sort(Comparator
                .comparing((BuiltMultiblockContext option) -> option.id().toString())
                .thenComparing(o -> o.shape().orElse(null), Comparator.nullsFirst(Comparator.naturalOrder())));
        return NameMap.of(options.getFirst(), options)
                .name(option -> {
                    var name = BuiltInRegistries.ITEM.get(option.id()).getDescription().copy();
                    if (option.shape().isPresent()) {
                        name.append(" (").append(option.shape().get()).append(")");
                    }
                    return name;
                })
                .icon(option -> ItemIcon.getItemIcon(BuiltInRegistries.ITEM.get(option.id())))
                .create();
    }

    @Nullable
    public static TaskType TYPE;

    private BuiltMultiblockContext multiblock;

    public BuiltMultiblockQuestTask(long id, Quest quest) {
        super(id, quest);
        multiblock = multiblockNameMap.get().values.getFirst();
    }

    @Override
    public TaskType getType() {
        return TYPE;
    }

    @Override
    public Icon getAltIcon() {
        var block = BuiltInRegistries.BLOCK.get(multiblock.id());
        return block == Blocks.AIR ? super.getAltIcon() : ItemIcon.getItemIcon(block.asItem());
    }

    @Override
    public Component getAltTitle() {
        var block = BuiltInRegistries.BLOCK.get(multiblock.id());
        if (block == Blocks.AIR) {
            return super.getAltTitle();
        }
        var name = block.getName();
        if (multiblock.shape().isPresent()) {
            name.append(" (").append(multiblock.shape().get()).append(")");
        }
        return MIText.BuildMultiblock.text(name);
    }

    public boolean canComplete(TeamData teamData) {
        return !teamData.isCompleted(this) && this.checkTaskSequence(teamData);
    }

    public boolean matches(BuiltMultiblockContext context) {
        return Objects.equals(multiblock.id(), context.id()) &&
                (multiblock.shape().isEmpty() || Objects.equals(multiblock.shape(), context.shape()));
    }

    @Override
    public boolean canSubmit(TeamData teamData, ServerPlayer serverPlayer) {
        // Don't allow the user to submit, since we require the multiblock building trigger to complete this task
        // If we allowed this, the task could be clicked to be completed regardless of actually completing the task
        return false;
    }

    @Override
    public void submitTask(TeamData teamData, ServerPlayer player, ItemStack craftedItem) {}

    @Override
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);
        nbt.put("multiblock", BuiltMultiblockContext.CODEC.encodeStart(NbtOps.INSTANCE, multiblock).getOrThrow());
    }

    @Override
    public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.readData(nbt, provider);
        multiblock = BuiltMultiblockContext.CODEC.decode(NbtOps.INSTANCE, nbt.get("multiblock")).getOrThrow().getFirst();
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        BuiltMultiblockContext.STREAM_CODEC.encode(buffer, multiblock);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        multiblock = BuiltMultiblockContext.STREAM_CODEC.decode(buffer);
    }

    @Override
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addEnum("machine", multiblock, v -> multiblock = v, multiblockNameMap.get(), multiblockNameMap.get().values.getFirst());
    }
}
