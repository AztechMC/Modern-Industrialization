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
package aztech.modern_industrialization.thirdparty.fabricrendering;
/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Arrays;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Contract;
import org.joml.Vector3f;

/**
 * Collection of utilities for model implementations.
 */
public abstract class ModelHelper {
    private ModelHelper() {
    }

    /** Result from {@link #toFaceIndex(Direction)} for null values. */
    public static final int NULL_FACE_ID = 6;

    /**
     * Convenient way to encode faces that may be null. Null is returned as {@link #NULL_FACE_ID}. Use
     * {@link #faceFromIndex(int)} to retrieve encoded face.
     */
    public static int toFaceIndex(Direction face) {
        return face == null ? NULL_FACE_ID : face.get3DDataValue();
    }

    /**
     * Use to decode a result from {@link #toFaceIndex(Direction)}. Return value will be null if encoded value was null.
     * Can also be used for no-allocation iteration of {@link Direction#values()}, optionally including the null face.
     * (Use &lt; or &lt;= {@link #NULL_FACE_ID} to exclude or include the null value, respectively.)
     */
    @Contract("null -> null")
    public static Direction faceFromIndex(int faceIndex) {
        return FACES[faceIndex];
    }

    /** @see #faceFromIndex(int) */
    private static final Direction[] FACES = Arrays.copyOf(Direction.values(), 7);

    /**
     * The vanilla model transformation logic is closely coupled with model deserialization. That does little good for
     * modded model loaders and procedurally generated models. This convenient construction method applies the same
     * scaling factors used for vanilla models. This means you can use values from a vanilla JSON file as inputs to this
     * method.
     */
    private static ItemTransform makeTransform(float rotationX, float rotationY, float rotationZ, float translationX,
            float translationY, float translationZ, float scaleX, float scaleY, float scaleZ) {
        Vector3f translation = new Vector3f(translationX, translationY, translationZ);
        translation.mul(0.0625f);
        translation = new Vector3f(
                Mth.clamp(translation.x, -5f, 5f),
                Mth.clamp(translation.y, -5f, 5f),
                Mth.clamp(translation.z, -5f, 5f));
        return new ItemTransform(new Vector3f(rotationX, rotationY, rotationZ), translation,
                new Vector3f(scaleX, scaleY, scaleZ));
    }

    public static final ItemTransform TRANSFORM_BLOCK_GUI = makeTransform(30, 225, 0, 0, 0, 0, 0.625f, 0.625f, 0.625f);
    public static final ItemTransform TRANSFORM_BLOCK_GROUND = makeTransform(0, 0, 0, 0, 3, 0, 0.25f, 0.25f, 0.25f);
    public static final ItemTransform TRANSFORM_BLOCK_FIXED = makeTransform(0, 0, 0, 0, 0, 0, 0.5f, 0.5f, 0.5f);
    public static final ItemTransform TRANSFORM_BLOCK_3RD_PERSON_RIGHT = makeTransform(75, 45, 0, 0, 2.5f, 0, 0.375f,
            0.375f, 0.375f);
    public static final ItemTransform TRANSFORM_BLOCK_1ST_PERSON_RIGHT = makeTransform(0, 45, 0, 0, 0, 0, 0.4f, 0.4f,
            0.4f);
    public static final ItemTransform TRANSFORM_BLOCK_1ST_PERSON_LEFT = makeTransform(0, 225, 0, 0, 0, 0, 0.4f, 0.4f,
            0.4f);

    /**
     * Mimics the vanilla model transformation used for most vanilla blocks, and should be suitable for most custom
     * block-like models.
     */
    public static final ItemTransforms MODEL_TRANSFORM_BLOCK = new ItemTransforms(TRANSFORM_BLOCK_3RD_PERSON_RIGHT,
            TRANSFORM_BLOCK_3RD_PERSON_RIGHT, TRANSFORM_BLOCK_1ST_PERSON_LEFT, TRANSFORM_BLOCK_1ST_PERSON_RIGHT,
            ItemTransform.NO_TRANSFORM, TRANSFORM_BLOCK_GUI, TRANSFORM_BLOCK_GROUND, TRANSFORM_BLOCK_FIXED);
}
