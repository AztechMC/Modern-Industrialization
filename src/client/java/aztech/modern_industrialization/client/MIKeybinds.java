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

package aztech.modern_industrialization.client;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.items.ActivatableItem;
import aztech.modern_industrialization.network.armor.ActivateItemPacket;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

public class MIKeybinds {
    private static final Set<Keybind> MAPPINGS = Sets.newHashSet();

    public static Set<Keybind> getMappings() {
        return Collections.unmodifiableSet(MAPPINGS);
    }

    public static void init(RegisterKeyMappingsEvent event) {
        MAPPINGS.forEach(m -> event.register(m.holder().get()));
    }

    public static final String CATEGORY = Util.makeDescriptionId("key.categories", MI.id(MI.ID));

    public static final Keybind TOGGLE_FLIGHT = create(
            "toggle_flight",
            id -> new KeyMapping(
                    id,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_V,
                    CATEGORY),
            toggleableItemAction(
                    EquipmentSlot.CHEST,
                    i -> i.is(MIItem.DIESEL_JETPACK.asItem()) || i.is(MIItem.GRAVICHESTPLATE.asItem()),
                    (player, activated) -> {}));

    public static final Keybind TOGGLE_3_BY_3 = create(
            "toggle_3x3",
            id -> new KeyMapping(
                    id,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_Y,
                    CATEGORY),
            toggleableItemAction(
                    EquipmentSlot.MAINHAND,
                    i -> i.is(MIItem.STEAM_MINING_DRILL.asItem()),
                    (player, activated) -> player.displayClientMessage((activated ? MIText.ToolSwitched3x3 : MIText.ToolSwitchedNo3x3).text(),
                            true)));

    private static Keybind create(String id, Function<String, KeyMapping> creator, Runnable action) {
        String descriptionId = Util.makeDescriptionId("key", MI.id(id));
        Keybind keybind = new Keybind(Lazy.of(() -> creator.apply(descriptionId)), action);
        MAPPINGS.add(keybind);
        return keybind;
    }

    private static Runnable toggleableItemAction(EquipmentSlot slot, Predicate<ItemStack> filter, BiConsumer<Player, Boolean> after) {
        return () -> {
            Player player = Minecraft.getInstance().player;
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof ActivatableItem item && filter.test(stack)) {
                boolean activated = !item.isActivated(stack);
                item.setActivated(stack, activated);
                new ActivateItemPacket(slot, activated).sendToServer();
                after.accept(player, activated);
            }
        };
    }

    public record Keybind(Lazy<KeyMapping> holder, Runnable action) {}
}
