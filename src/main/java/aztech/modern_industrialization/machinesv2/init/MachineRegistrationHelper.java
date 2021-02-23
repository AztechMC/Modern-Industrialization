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
package aztech.modern_industrialization.machinesv2.init;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machinesv2.MachineBlock;
import aztech.modern_industrialization.util.ResourceUtil;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MachineRegistrationHelper {
    /**
     * Register a machine's block, block entity type and wrenchable tag.
     * 
     * @param id               Machine block id, for example "lv_macerator"
     * @param factory          The block entity constructor, with a BET parameter.
     * @param extraRegistrator A BET consumer used for API registration.
     */
    public static void registerMachine(String id, Function<BlockEntityType<?>, BlockEntity> factory, Consumer<BlockEntityType<?>> extraRegistrator) {
        BlockEntityType<?>[] bet = new BlockEntityType[1];
        Supplier<BlockEntity> ctor = () -> factory.apply(bet[0]);
        Block block = new MachineBlock(id, ctor);
        bet[0] = Registry.register(Registry.BLOCK_ENTITY_TYPE, new MIIdentifier(id), BlockEntityType.Builder.create(ctor, block).build(null));
        ResourceUtil.appendToTag(new Identifier("fabric:blocks/wrenchables"), new MIIdentifier(id));
        extraRegistrator.accept(bet[0]);
    }
}
