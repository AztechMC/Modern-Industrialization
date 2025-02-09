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
package aztech.modern_industrialization.test.framework;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.test.FluidPipeTests;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;

public final class MIGameTests {
    private MIGameTests() {
    }

    private static final List<Class<?>> TEST_CLASSES = List.of(
            FluidPipeTests.class);

    @GameTestGenerator
    public static List<TestFunction> generateTests() {
        var result = new ArrayList<TestFunction>();

        for (var testClass : TEST_CLASSES) {
            for (var testMethod : testClass.getMethods()) {
                var gametest = testMethod.getAnnotation(MIGameTest.class);
                if (gametest == null) {
                    continue;
                }

                result.add(new TestFunction(
                        gametest.batch(),
                        MI.ID + "." + testMethod.getName().toLowerCase(Locale.ROOT),
                        MI.id("empty").toString(),
                        StructureUtils.getRotationForRotationSteps(gametest.rotationSteps()),
                        gametest.timeoutTicks(),
                        gametest.setupTicks(),
                        gametest.required(),
                        gametest.manualOnly(),
                        gametest.attempts(),
                        gametest.requiredSuccesses(),
                        gametest.skyAccess(),
                        gameTestHelper -> {
                            try {
                                var testObject = testClass.getConstructor().newInstance();
                                testMethod.invoke(testObject, new MIGameTestHelper(gameTestHelper.testInfo));
                            } catch (ReflectiveOperationException e) {
                                throw new RuntimeException(e);
                            }
                        }));
            }
        }

        return result;
    }
}
