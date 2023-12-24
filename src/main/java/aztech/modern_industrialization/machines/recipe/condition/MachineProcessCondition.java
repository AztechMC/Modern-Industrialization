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

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.ApiStatus;

public interface MachineProcessCondition {
    boolean canProcessRecipe(Context context, MachineRecipe recipe);

    void appendDescription(List<Component> list);

    Serializer<?> getSerializer();

    @ApiStatus.NonExtendable
    default JsonObject toJson() {
        var obj = ((Serializer) getSerializer()).toJson(this, false);
        obj.addProperty("id", MachineProcessConditions.getId(getSerializer()).toString());
        return obj;
    }

    interface Context {
        MachineBlockEntity getBlockEntity();

        default ServerLevel getLevel() {
            return (ServerLevel) getBlockEntity().getLevel();
        }
    }

    interface Serializer<T extends MachineProcessCondition> {
        T fromJson(JsonObject json);

        /**
         * @param syncToClient False if writing to datapack, true if writing to client.
         */
        JsonObject toJson(T condition, boolean syncToClient);
    }
}
