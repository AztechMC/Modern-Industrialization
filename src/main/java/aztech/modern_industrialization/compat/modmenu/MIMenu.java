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
package aztech.modern_industrialization.compat.modmenu;

import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.materials.Material;
import aztech.modern_industrialization.materials.MaterialRegistry;
import aztech.modern_industrialization.materials.part.MIParts;
import aztech.modern_industrialization.materials.part.OrePart;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class MIMenu implements ModMenuApi {

    private static final ConfigEntryBuilder ENTRY_BUILDER = ConfigEntryBuilder.create();

    private static boolean hasOreGen(Material material) {
        return OrePart.GENERATED_MATERIALS.contains(material.name);
    }

    private static String getOreTranslationKey(Material material) {
        return getOreItem(material).getDescriptionId();
    }

    private static Item getOreItem(Material material) {
        return Registry.ITEM.get(new ResourceLocation(material.getParts().get(MIParts.ORE.key).getItemId()));
    }

    private static boolean oreInList(List<String> list, Material material) {
        return list.contains(material.name);
    }

    private static void setOreInList(List<String> list, Material material, boolean inList) {
        if (inList) {
            if (!list.contains(material.name)) {
                list.add(material.name);
            }
        } else {
            list.remove(material.name);
        }
    }

    private static boolean oreNotInList(Field field, Object config, Material material) {
        List<String> list;
        try {
            list = (List<String>) field.get(config);
            return !oreInList(list, material);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static void setOreInList(Field field, Object config, Material material, boolean inList) {
        List<String> list;
        try {
            list = (List<String>) field.get(config);
            setOreInList(list, material, inList);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    static {
        GuiRegistry registry = AutoConfig.getGuiRegistry(MIConfig.class);
        registry.registerAnnotationProvider(
                (i13n, field, config, defaults,
                        registry1) -> Collections.singletonList(ENTRY_BUILDER
                                .startSubCategory(MIText.CustomOreGen.text(),
                                        MaterialRegistry.getMaterials().values().stream().filter(MIMenu::hasOreGen)
                                                .map(i -> new CustomBooleanListEntry(new TranslatableComponent(getOreTranslationKey(i)),
                                                        oreNotInList(field, config, i), () -> oreNotInList(field, config, i),
                                                        bool -> setOreInList(field, config, i, !bool), getOreItem(i)))
                                                .collect(Collectors.toList()))
                                .build()),
                OreConfigEntry.class);
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(MIConfig.class, parent).get();
    }
}
