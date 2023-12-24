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
package aztech.modern_industrialization.machines.recipe.condition;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;

public class AdjacentBlockProcessCondition implements MachineProcessCondition {
    public static final Serde SERIALIZER = new Serde();

    private final Block block;
    private final RelativePosition relativePosition;

    public AdjacentBlockProcessCondition(Block block, String relativePosition) {
        this.block = block;
        this.relativePosition = switch (relativePosition) {
        case "below" -> RelativePosition.BELOW;
        case "behind" -> RelativePosition.BEHIND;
        default -> throw new IllegalArgumentException("Invalid position: " + relativePosition);
        };
    }

    @Override
    public boolean canProcessRecipe(Context context, MachineRecipe recipe) {
        var checkPos = switch (relativePosition) {
        case BELOW -> context.getBlockEntity().getBlockPos().below();
        case BEHIND -> {
            var direction = context.getBlockEntity().orientation.facingDirection;
            yield context.getBlockEntity().getBlockPos().relative(direction.getOpposite());
        }
        };
        return context.getLevel().getBlockState(checkPos).is(block);
    }

    @Override
    public void appendDescription(List<Component> list) {
        var text = switch (relativePosition) {
        case BELOW -> MIText.RequiresBlockBelow;
        case BEHIND -> MIText.RequiresBlockBehind;
        };
        list.add(text.text(block.getName()));
    }

    @Override
    public Serializer<?> getSerializer() {
        return SERIALIZER;
    }

    private static class Serde implements Serializer<AdjacentBlockProcessCondition> {
        @Override
        public AdjacentBlockProcessCondition fromJson(JsonObject json) {
            var blockId = new ResourceLocation(GsonHelper.getAsString(json, "block"));
            var block = BuiltInRegistries.BLOCK.getOptional(blockId).orElseThrow(() -> new IllegalArgumentException("Invalid block: " + blockId));
            var pos = GsonHelper.getAsString(json, "position");
            return new AdjacentBlockProcessCondition(block, pos);
        }

        @Override
        public JsonObject toJson(AdjacentBlockProcessCondition condition, boolean syncToClient) {
            var obj = new JsonObject();
            obj.addProperty("block", BuiltInRegistries.BLOCK.getKey(condition.block).toString());
            obj.addProperty("position", condition.relativePosition.toString());
            return obj;
        }
    }
}

enum RelativePosition {
    BELOW,
    BEHIND;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
