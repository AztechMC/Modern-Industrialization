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
package aztech.modern_industrialization.materials.part;

import aztech.modern_industrialization.api.item.modular_tools.ComponentTier;
import aztech.modern_industrialization.api.item.modular_tools.HeadRegistry;
import aztech.modern_industrialization.api.item.modular_tools.HeadRegistry.HeadProperties;
import aztech.modern_industrialization.items.modulartools.ModularToolItem.ToolType;

public class DrillHeadPart implements PartKeyProvider {
    @Override
    public PartKey key() {
        return new PartKey("drill_head");
    }

    public PartTemplate simple() {
        return new PartTemplate("Drill Head", "drill_head");
    }

    public PartTemplate withModularComponent(ComponentTier tier, int miningLevel, float miningSpeed,
            double attackDamage) {
        return new PartTemplate("Drill Head", "drill_head").withRegister(
                (partContext, part, itemPath, itemId, itemTag, englishName) -> {
                    var item = PartTemplate.createSimpleItem(englishName, itemPath, partContext, part);
                    HeadRegistry.register(item,
                            new HeadProperties(tier, ToolType.DRILL, miningLevel, miningSpeed, attackDamage));
                });
    }
}
