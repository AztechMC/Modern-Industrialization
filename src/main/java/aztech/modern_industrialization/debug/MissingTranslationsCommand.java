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
import net.minecraft.advancement.Advancement;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MissingTranslationsCommand {
    public static int run(CommandContext<FabricClientCommandSource> context) {
        if (dumpTranslations()) {
            context.getSource().sendFeedback(new LiteralText("Successfully dumped missing translations!"));
        } else {
            context.getSource().sendFeedback(new LiteralText("No missing translations!"));
        }
        return 1;
    }

    /**
     * Dump missing translation and return true if something was dumped.
     */
    private static boolean dumpTranslations() {
        Map<String, String> missingTranslations = new LinkedHashMap<>();
        for (Block block : Registry.BLOCK) {
            Identifier id = Registry.BLOCK.getId(block);
            if (id.getNamespace().equals("modern_industrialization")) {
                String key = block.getTranslationKey();
                if (!I18n.hasTranslation(key)) {
                    missingTranslations.put(key, "XXX");
                }
            }
        }
        for (Item item : Registry.ITEM) {
            Identifier id = Registry.ITEM.getId(item);
            if (id.getNamespace().equals("modern_industrialization")) {
                String key = item.getTranslationKey();
                if (!I18n.hasTranslation(key)) {
                    missingTranslations.put(key, "XXX");
                }
            }
        }
        for (Fluid fluid : Registry.FLUID) {
            Identifier id = Registry.FLUID.getId(fluid);
            if (id.getNamespace().equals("modern_industrialization")) {
                String key = fluid.getDefaultState().getBlockState().getBlock().getTranslationKey();
                if (!I18n.hasTranslation(key)) {
                    missingTranslations.put(key, "XXX");
                }
            }
        }

        for (Map.Entry<String, String> entry : missingTranslations.entrySet()) {

            entry.setValue(fit(entry.getKey()));
        }

        for (Advancement advancement : MinecraftClient.getInstance().getNetworkHandler().getAdvancementHandler().getManager().getAdvancements()) {

            if (advancement.getId().getNamespace().equals("modern_industrialization")) {
                String key = "advancements.modern_industrialization." + advancement.getId().getPath();
                String keyDescription = "advancements.modern_industrialization." + advancement.getId().getPath() + ".description";

                if (!I18n.hasTranslation(key)) {
                    missingTranslations.put(key, "XXX");
                }

                if (!I18n.hasTranslation(keyDescription)) {
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
