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

package aztech.modern_industrialization.compat.ftbquests;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.advancement.multiblock.BuiltMultiblockContext;
import aztech.modern_industrialization.compat.ftbquests.task.BuiltMultiblockQuestTask;
import aztech.modern_industrialization.config.MIServerConfig;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.events.ClearFileCacheEvent;
import dev.ftb.mods.ftbquests.item.MissingItem;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public class FTBQuestsFacadeImpl implements FTBQuestsFacade {
    @Nullable
    private Map<Item, List<ItemTask>> itemTasks;
    @Nullable
    private List<BuiltMultiblockQuestTask> builtMultiblockTasks;

    @Override
    public void init() {
        BuiltMultiblockQuestTask.TYPE = TaskTypes.register(MI.id("built_multiblock"), BuiltMultiblockQuestTask::new, () -> Icon.getIcon("modern_industrialization:item/wrench"));

        ClearFileCacheEvent.EVENT.register(event -> this.invalidateCaches());
    }

    private void invalidateCaches() {
        itemTasks = null;
        builtMultiblockTasks = null;
    }

    private Map<Item, List<ItemTask>> getItemTasks() {
        if (itemTasks == null) {
            itemTasks = ServerQuestFile.INSTANCE.collect(ItemTask.class).stream()
                    .filter(task -> !(task.getItemStack().getItem() instanceof MissingItem) && !task.consumesResources())
                    .collect(Collectors.groupingBy(task -> task.getItemStack().getItem()));
        }
        return itemTasks;
    }

    private List<ItemTask> getItemTasks(Item item) {
        var tasks = getItemTasks().get(item);
        return tasks == null ? List.of() : tasks;
    }

    private List<BuiltMultiblockQuestTask> getBuiltMultiblockTasks() {
        if (builtMultiblockTasks == null) {
            builtMultiblockTasks = ServerQuestFile.INSTANCE.collect(BuiltMultiblockQuestTask.class);
        }
        return builtMultiblockTasks;
    }

    @Override
    public void addCompleted(UUID uuid, Item item, long amount) {
        if (!MIServerConfig.INSTANCE.ftbQuestsIntegration.getAsBoolean() ||
                item instanceof MissingItem) {
            return;
        }

        var file = ServerQuestFile.INSTANCE;
        var team = FTBTeamsAPI.api().getManager().getTeamForPlayerID(uuid).orElse(null);
        if (team == null) {
            return;
        }
        var data = file.getNullableTeamData(team.getId());

        if (data == null || data.isLocked()) {
            return;
        }

        var stack = new ItemStack(item, (int) amount);

        for (var task : getItemTasks(item)) {
            if (data.canStartTasks(task.getQuest()) && !data.isCompleted(task) && task.test(stack)) {
                data.addProgress(task, amount);
            }
        }
    }

    @Override
    public void addCompleted(UUID uuid, Fluid fluid, long amount) {
        if (!MIServerConfig.INSTANCE.ftbQuestsIntegration.getAsBoolean()) {
            return;
        }

        var bucketItem = fluid.getBucket();
        if (bucketItem == null || bucketItem == Items.AIR) {
            return;
        }

        addCompleted(uuid, fluid.getBucket(), 1);
    }

    @Override
    public void builtMultiblock(ServerPlayer player, BuiltMultiblockContext context) {
        var file = ServerQuestFile.INSTANCE;
        file.getTeamData(player).ifPresent(data -> {
            if (!data.isLocked()) {
                file.withPlayerContext(player, () -> {
                    for (var task : getBuiltMultiblockTasks()) {
                        if (data.canStartTasks(task.getQuest()) && task.canComplete(data) && task.matches(context)) {
                            data.setProgress(task, 1);
                        }
                    }
                });
            }
        });
    }
}
