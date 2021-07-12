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
import aztech.modern_industrialization.materials.Material;
import aztech.modern_industrialization.materials.MaterialRegistry;
import aztech.modern_industrialization.materials.part.MIParts;
import aztech.modern_industrialization.materials.part.MaterialPart;
import com.mojang.brigadier.context.CommandContext;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class DumpUnificationScriptCommand {
    public static int run(CommandContext<FabricClientCommandSource> context) {
        // Collect all common tag -> MI item id and other mod id -> MI item id mappings.
        TagGroup<Item> tagGroup = MinecraftClient.getInstance().player.networkHandler.getTagManager().getOrCreateTagGroup(Registry.ITEM_KEY);
        Map<String, String> tagToMi = new LinkedHashMap<>();
        Map<String, String> idToMi = new LinkedHashMap<>();
        Map<String, Map<String, String>> taggedMiParts = new LinkedHashMap<>();

        for (Map.Entry<String, Material> entry : MaterialRegistry.getMaterials().entrySet()) {
            Material material = entry.getValue();
            for (Map.Entry<String, MaterialPart> partEntry : material.getParts().entrySet()) {
                MaterialPart part = partEntry.getValue();
                boolean orePart = partEntry.getKey().equals(MIParts.ORE) || partEntry.getKey().equals(MIParts.ORE_DEEPLSATE);

                if (orePart && material.name.equals("quartz")) {
                    // Don't try to unify quartz or it breaks nether quartz.
                    continue;
                }

                // if part has tag, and not ore part
                if (!part.getTaggedItemId().equals(part.getItemId())) {
                    boolean foundOtherInTag = false;

                    Tag<Item> itemTag = tagGroup.getTag(new Identifier(part.getTaggedItemId().substring(1)));

                    if (itemTag != null) {
                        for (Item item : itemTag.values()) {
                            Identifier itemId = Registry.ITEM.getId(item);

                            if (!itemId.getNamespace().equals("minecraft") &&!itemId.getNamespace().equals("modern_industrialization")) {
                                if (orePart) {
                                    // Make sure we unify ores to the correct variant.
                                    boolean miDeepslate = partEntry.getKey().equals(MIParts.ORE_DEEPLSATE);
                                    boolean otherDeepslate = itemId.toString().contains("deepslate");
                                    if (miDeepslate != otherDeepslate) {
                                        continue;
                                    }
                                }
                                idToMi.put(itemId.toString(), part.getItemId());
                                foundOtherInTag = true;
                            }
                        }
                    }

                    if (foundOtherInTag || orePart) { // Ore parts need it for their special input unification.
                        if (!orePart) {
                            // Don't unify ore tagged inputs.
                            tagToMi.put(part.getTaggedItemId(), part.getItemId());
                        }
                        taggedMiParts.computeIfAbsent(part.getPart(), p -> new LinkedHashMap<>()).put(material.name, part.getItemId());
                    }
                }
            }
        }

        // Dump all of this
        StringBuilder serverScript = new StringBuilder("\n");
        serverScript.append("////////////////////////////////////////////////////////////////////////////////\n");
        serverScript.append("// Automatically generated MI unification script, paste in server_scripts/.   //\n");
        serverScript.append("// Run with `/miclient dump_unification_script`, and view it in your console. //\n");
        serverScript.append("////////////////////////////////////////////////////////////////////////////////\n");
        appendMap(serverScript, "tagToMi", tagToMi);
        appendMap(serverScript, "idToMi", idToMi);
        appendNestedMap(serverScript, "taggedMiParts", taggedMiParts);

        serverScript.append("""
                events.listen("recipes", function (event) {
                    // Replace untagged inputs / outputs with MI items.
                    for (var id in idToMi) {
                        event.replaceInput({}, id, idToMi[id]);
                        event.replaceOutput({}, id, idToMi[id]);
                    }
                    // Replace tagged inputs with MI inputs. (exluding ores)
                    for (var tag in tagToMi) {
                        event.replaceInput({}, tag, tagToMi[tag]);
                    }
                    // Replace ore tagged inputs.
                    for (var material in taggedMiParts["ore"]) {
                        var oreTag = "#c:" + material + "_ores";
                        if (material in taggedMiParts["ore_deepslate"]) {
                            var regularOre = taggedMiParts["ore"][material];
                            var deepslateOre = taggedMiParts["ore_deepslate"][material];
                            event.replaceInput({}, oreTag, [regularOre, deepslateOre]);
                        }
                    }
                    // Remove duplicate recipes
                    function autoremove(partName, recipePattern) {
                        for (material in taggedMiParts[partName]) {
                            event.remove({ id: recipePattern.replace("{}", material) });
                        }
                    }
                    // TR recipes
                    autoremove("block", "techreborn:crafting_table/storage_block/{}_storage_block");
                    autoremove("ingot", "techreborn:crafting_table/ingot/{}_ingot_from_block");
                    autoremove("ingot", "techreborn:crafting_table/ingot/{}_ingot_from_storage_block");
                    autoremove("ingot", "techreborn:crafting_table/ingot/{}_ingot_from_nugget");
                    autoremove("nugget", "techreborn:crafting_table/nugget/{}_nugget");
                    autoremove("ingot", "techreborn:smelting/{}_ingot_from_ore");
                    // Some duplicate MI recipes (not needed usually because we don't tag furnace inputs).
                    autoremove("ingot", "modern_industrialization:generated/materials/{}/smelting/ore_deepslate_to_ingot_smelting");
                    autoremove("ingot", "modern_industrialization:generated/materials/{}/smelting/ore_deepslate_to_ingot_blasting");
                });
                                                """);

        ModernIndustrialization.LOGGER.info("Outputting KubeJS server-side unification script:");
        ModernIndustrialization.LOGGER.info(serverScript.toString());

        // Dump all of this
        StringBuilder clientScript = new StringBuilder("\n");
        clientScript.append("////////////////////////////////////////////////////////////////////////////////\n");
        clientScript.append("// Automatically generated MI unification script, paste in client_scripts/.   //\n");
        clientScript.append("// Run with `/miclient dump_unification_script`, and view it in your console. //\n");
        clientScript.append("////////////////////////////////////////////////////////////////////////////////\n");
        appendMap(clientScript, "idToMiClient", idToMi);

        clientScript.append("""
                events.listen("mi_rei", function (event) {
                    for (var id in idToMiClient) {
                        event.remove(id);
                    }
                });
                                """);

        ModernIndustrialization.LOGGER.info("Outputting KubeJS client-side unification script:");
        ModernIndustrialization.LOGGER.info(clientScript.toString());

        context.getSource().sendFeedback(new LiteralText("Successfully dumped the unification scripts!"));

        return 1;
    }

    private static void appendMap(StringBuilder outputScript, String variableName, Map<String, String> mapContents) {
        outputScript.append("var ").append(variableName).append(" = {\n");

        for (Map.Entry<String, String> entry : mapContents.entrySet()) {
            outputScript.append("    \"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\",\n");
        }

        outputScript.append("};\n");
    }

    private static void appendNestedMap(StringBuilder outputScript, String variableName, Map<String, Map<String, String>> mapContents) {
        outputScript.append("var ").append(variableName).append(" = {\n");

        for (Map.Entry<String, Map<String, String>> entry : mapContents.entrySet()) {
            outputScript.append("    \"").append(entry.getKey()).append("\": {\n");
            for (Map.Entry<String, String> nestedEntry : entry.getValue().entrySet()) {
                outputScript.append("        \"").append(nestedEntry.getKey()).append("\": \"").append(nestedEntry.getValue()).append("\",\n");
            }
            outputScript.append("    },\n");
        }

        outputScript.append("};\n");
    }
}
