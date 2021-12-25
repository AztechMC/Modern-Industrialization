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
package aztech.modern_industrialization.debug;

import aztech.modern_industrialization.ModernIndustrialization;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class MissingTranslationsCommand {
    public static int run(CommandContext<FabricClientCommandSource> context) {
        if (dumpTranslations()) {
            context.getSource().sendFeedback(new TextComponent("Successfully dumped missing translations!"));
        } else {
            context.getSource().sendFeedback(new TextComponent("No missing translations!"));
        }
        return 1;
    }

    /**
     * Dump missing translation and return true if something was dumped.
     */
    private static boolean dumpTranslations() {
        Map<String, String> missingTranslations = new LinkedHashMap<>();
        for (Block block : Registry.BLOCK) {
            ResourceLocation id = Registry.BLOCK.getKey(block);
            if (id.getNamespace().equals("modern_industrialization")) {
                String key = block.getDescriptionId();
                if (!I18n.exists(key)) {
                    missingTranslations.put(key, "XXX");
                }
            }
        }
        for (Item item : Registry.ITEM) {
            ResourceLocation id = Registry.ITEM.getKey(item);
            if (id.getNamespace().equals("modern_industrialization")) {
                String key = item.getDescriptionId();
                if (!I18n.exists(key)) {
                    missingTranslations.put(key, "XXX");
                }
            }
        }
        for (Fluid fluid : Registry.FLUID) {
            ResourceLocation id = Registry.FLUID.getKey(fluid);
            if (id.getNamespace().equals("modern_industrialization")) {
                String key = fluid.defaultFluidState().createLegacyBlock().getBlock().getDescriptionId();
                if (!I18n.exists(key)) {
                    missingTranslations.put(key, "XXX");
                }
            }
        }

        for (Map.Entry<String, String> entry : missingTranslations.entrySet()) {

            entry.setValue(fit(entry.getKey()));
        }

        for (Advancement advancement : Minecraft.getInstance().getConnection().getAdvancements().getAdvancements().getAllAdvancements()) {

            if (advancement.getId().getNamespace().equals("modern_industrialization")) {
                String key = "advancements.modern_industrialization." + advancement.getId().getPath();
                String keyDescription = "advancements.modern_industrialization." + advancement.getId().getPath() + ".description";

                if (!I18n.exists(key)) {
                    missingTranslations.put(key, "XXX");
                }

                if (!I18n.exists(keyDescription)) {
                    String[] toTranslate = advancement.getId().getPath().split("_");
                    StringBuilder translated = new StringBuilder();
                    boolean first = true;
                    for (String toTranslatePart : toTranslate) {
                        if (!first)
                            translated.append(" ");
                        first = false;
                        // Uppercase first char
                        translated.append(Character.toUpperCase(toTranslatePart.charAt(0)));
                        // Rest is lowercase
                        translated.append(toTranslatePart.substring(1));
                    }

                    missingTranslations.put(keyDescription, "Craft a " + translated);
                }
            }
        }

        if (missingTranslations.size() > 0) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            ModernIndustrialization.LOGGER.info("Missing MI translations:\n{}", gson.toJson(missingTranslations));
            return true;
        } else {
            return false;
        }
    }

    public static String fit(String key) {
        String[] parts = key.split("\\.");
        String[] toTranslate = parts[2].split("_");
        StringBuilder translated = new StringBuilder();
        boolean first = true;
        for (String toTranslatePart : toTranslate) {
            if (!first)
                translated.append(" ");
            first = false;
            // Uppercase first char
            translated.append(Character.toUpperCase(toTranslatePart.charAt(0)));
            // Rest is lowercase
            translated.append(toTranslatePart.substring(1));
        }
        return translated.toString();
    }
}
