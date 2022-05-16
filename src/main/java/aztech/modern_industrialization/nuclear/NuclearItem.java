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
package aztech.modern_industrialization.nuclear;

import aztech.modern_industrialization.ModernIndustrialization;
import net.minecraft.world.item.Item;

public class NuclearItem {

    public static void init() {
        ModernIndustrialization.LOGGER.info("Setting up Nuclear Items");
    }

    public static final Item SMALL_HEAT_EXCHANGER = NuclearComponentItem.of(
            "Small Heat Exchanger",
            "small_heat_exchanger", 2500, 15 * NuclearConstant.BASE_HEAT_CONDUCTION,
            INeutronBehaviour.NO_INTERACTION);

    public static final Item LARGE_HEAT_EXCHANGER = NuclearComponentItem.of(
            "Large Heat Exchanger",
            "large_heat_exchanger", 1800, 30 * NuclearConstant.BASE_HEAT_CONDUCTION,
            INeutronBehaviour.NO_INTERACTION);
}
