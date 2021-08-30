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
package aztech.modern_industrialization.mixin_impl.plugin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

class TooltipComponentAsm {
    private static final String TARGET_METHOD_DESC = String
            .format("(L%s;)L%s;", FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "net.minecraft.class_5632"), // TooltipData
                    FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "net.minecraft.class_5684") // TooltipComponent
            ).replace('.', '/');

    static void apply(ClassNode targetClass) {
        MethodNode method = findOfMethod(targetClass);
        method.instructions.insertBefore(findNew(method.instructions), createInjection());
    }

    private static MethodNode findOfMethod(ClassNode node) {
        return node.methods.stream().filter(methodNode -> methodNode.desc.equals(TARGET_METHOD_DESC)).findFirst().get();
    }

    private static AbstractInsnNode findNew(InsnList list) {
        for (AbstractInsnNode node : list) {
            if (node instanceof TypeInsnNode ty && ty.getOpcode() == Opcodes.NEW && ty.desc.equals("java/lang/IllegalArgumentException")) {
                return node;
            }
        }

        throw new RuntimeException();
    }

    private static InsnList createInjection() {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "aztech/modern_industrialization/mixin_impl/MITooltipComponents", "of", TARGET_METHOD_DESC,
                false));
        list.add(new InsnNode(Opcodes.DUP));
        Label ifNull = new Label();
        LabelNode ifNullNode = new LabelNode(ifNull);
        list.add(new JumpInsnNode(Opcodes.IFNULL, ifNullNode));
        list.add(new InsnNode(Opcodes.ARETURN));
        list.add(ifNullNode);
        list.add(new InsnNode(Opcodes.POP));
        return list;
    }
}
