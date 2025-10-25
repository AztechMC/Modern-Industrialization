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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Grants the vanilla "Wax On" advancement on successful usage.
 */
public class WaxItem extends HoneycombItem {
    public WaxItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var ret = super.useOn(context);
        if (ret.indicateItemUse()) {
            if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                grantWaxOn(serverPlayer);
            }
        }
        return ret;
    }

    private static final ResourceLocation WAX_ON = ResourceLocation.withDefaultNamespace("husbandry/wax_on");

    private static void grantWaxOn(ServerPlayer serverPlayer) {
        var advancement = serverPlayer.server.getAdvancements().get(WAX_ON);
        if (advancement == null) {
            return;
        }

        var playerAdvancements = serverPlayer.server.getPlayerList().getPlayerAdvancements(serverPlayer);
        playerAdvancements.award(advancement, "wax_on");
    }
}
