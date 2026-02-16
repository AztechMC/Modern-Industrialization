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

package aztech.modern_industrialization.trading;

import aztech.modern_industrialization.MI;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.trading.VillagerTrade;

public class MITradeTags {
    public static final TagKey<VillagerTrade> INDUSTRIALIST_LEVEL_1 = tag("industrialist/level_1");
    public static final TagKey<VillagerTrade> INDUSTRIALIST_LEVEL_2 = tag("industrialist/level_2");
    public static final TagKey<VillagerTrade> INDUSTRIALIST_LEVEL_3 = tag("industrialist/level_3");
    public static final TagKey<VillagerTrade> INDUSTRIALIST_LEVEL_4 = tag("industrialist/level_4");
    public static final TagKey<VillagerTrade> INDUSTRIALIST_LEVEL_5 = tag("industrialist/level_5");

    private static TagKey<VillagerTrade> tag(String path) {
        return TagKey.create(Registries.VILLAGER_TRADE, MI.id(path));
    }
}
