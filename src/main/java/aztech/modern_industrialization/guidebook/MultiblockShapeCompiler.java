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

package aztech.modern_industrialization.guidebook;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import guideme.color.ConstantColor;
import guideme.color.SymbolicColor;
import guideme.compiler.PageCompiler;
import guideme.compiler.tags.MdxAttrs;
import guideme.document.LytErrorSink;
import guideme.document.interaction.TextTooltip;
import guideme.libs.mdast.mdx.model.MdxJsxElementFields;
import guideme.scene.GuidebookScene;
import guideme.scene.annotation.InWorldBoxAnnotation;
import guideme.scene.element.SceneElementTagCompiler;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class MultiblockShapeCompiler implements SceneElementTagCompiler {
    @Override
    public Set<String> getTagNames() {
        return Set.of("MultiblockShape");
    }

    @Override
    public void compile(GuidebookScene scene, PageCompiler compiler, LytErrorSink errorSink, MdxJsxElementFields el) {
        var controller = MdxAttrs.getRequiredBlockAndId(compiler, errorSink, el, "controller");
        if (controller == null) {
            // Parsing error
            return;
        }
        if (!(controller.getRight() instanceof MachineBlock machineBlock)
                || !(machineBlock.getBlockEntityInstance() instanceof MultiblockMachineBlockEntity multi)) {
            errorSink.appendError(compiler, "Block is not a multiblock controller: " + controller.getLeft(), el);
            return;
        }
        var useBigShape = MdxAttrs.getBoolean(compiler, errorSink, el, "useBigShape", false);

        var shape = useBigShape ? multi.getBigShape() : multi.getActiveShape();
        var controllerPos = MdxAttrs.getPos(compiler, errorSink, el);

        // Controller
        scene.getLevel().setBlockAndUpdate(controllerPos, controller.getRight().defaultBlockState());
        // Shape blocks
        for (var entry : shape.simpleMembers.entrySet()) {
            var pos = ShapeMatcher.toWorldPos(controllerPos, multi.orientation.facingDirection, entry.getKey());
            var state = ShapeMatcher.toWorldState(scene.getLevel(), pos, entry.getValue().getPreviewState(), multi.orientation.facingDirection);
            scene.getLevel().setBlockAndUpdate(pos, state);

            // Annotations for allowed blocks and hatches
            var member = shape.simpleMembers.get(entry.getKey());
            var ingredient = member.getItemPreviewState(scene.getLevel().registryAccess());
            List<Component> tooltipLines = new ArrayList<>(member.getTooltipPreview(scene.getLevel().registryAccess()));
            var hatch = shape.hatchFlags.get(entry.getKey());
            boolean hasHatches = hatch != null && !hatch.values().isEmpty();
            if (hasHatches) {
                tooltipLines.add(MIText.AcceptsHatches.text());
                for (var type : hatch.values()) {
                    tooltipLines.add(Component.literal("- ").append(type.description()));
                }
            }
            var minCorner = Vec3.atLowerCornerOf(pos);
            var annotation = new InWorldBoxAnnotation(minCorner.toVector3f(), minCorner.add(1, 1, 1).toVector3f(), hasHatches ? SymbolicColor.GREEN : ConstantColor.TRANSPARENT);
            annotation.setTooltip(new TextTooltip(tooltipLines));
            scene.addAnnotation(annotation);
        }
    }
}
