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
import aztech.modern_industrialization.config.MIStartupConfig;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.util.Lazy;
import dev.ftb.mods.ftbquests.events.ClearFileCacheEvent;
import dev.ftb.mods.ftbquests.item.MissingItem;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FTBQuestsFacadeImpl implements FTBQuestsFacade {
    private final Lazy<List<BuiltMultiblockQuestTask>> builtMultiblockTasks = Lazy.of(() -> ServerQuestFile.INSTANCE.collect(BuiltMultiblockQuestTask.class));

    @Override
    public void init() {
        BuiltMultiblockQuestTask.TYPE = TaskTypes.register(MI.id("built_multiblock"), BuiltMultiblockQuestTask::new, () -> Icon.getIcon("modern_industrialization:item/wrench"));

        ClearFileCacheEvent.EVENT.register(event -> this.invalidateCaches());
    }

    private void invalidateCaches() {
        builtMultiblockTasks.invalidate();
    }

    @Override
    public void addCompleted(UUID uuid, Item item, long amount) {
        if (!MIStartupConfig.INSTANCE.ftbQuestsIntegration.getAsBoolean()) {
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

        ItemStack stack = new ItemStack(item, (int) amount);

        for (var task : file.getSubmitTasks()) {
            if (task instanceof ItemTask itemTask && data.canStartTasks(task.getQuest())) {
                if (data.isCompleted(task) || itemTask.getItemStack().getItem() instanceof MissingItem || item instanceof MissingItem) {
                    continue;
                }

                if (!task.consumesResources() && itemTask.test(stack)) {
                    data.addProgress(task, amount);
                }
            }
        }
    }

    @Override
    public void builtMultiblock(ServerPlayer player, BuiltMultiblockContext context) {
        var file = ServerQuestFile.INSTANCE;
        file.getTeamData(player).ifPresent(data -> {
            if (!data.isLocked()) {
                file.withPlayerContext(player, () -> {
                    for (var task : builtMultiblockTasks.get()) {
                        if (data.canStartTasks(task.getQuest()) && task.canComplete(data) && task.matches(context)) {
                            data.setProgress(task, 1);
                        }
                    }
                });
            }
        });
    }
}
