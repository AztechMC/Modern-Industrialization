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

package aztech.modern_industrialization.config;

import aztech.modern_industrialization.MI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.CheckReturnValue;

public class MIConfigBuilder {
    public static final Map<String, String> configTranslations = new ConcurrentHashMap<>();

    private static String configTranslationKey(String key) {
        return MI.ID + ".configuration." + key;
    }

    private final ModConfigSpec.Builder builder;

    public MIConfigBuilder() {
        this.builder = new ModConfigSpec.Builder();
    }

    public void pushSection(String key, String title) {
        String sectionTranslation = configTranslationKey(key);
        configTranslations.put(sectionTranslation, title);
        builder.push(key);
    }

    public void popSection() {
        builder.pop();
    }

    @CheckReturnValue
    public ModConfigSpec.Builder start(String key, String title, String... comment) {
        if (comment.length == 0) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }

        var translationKey = configTranslationKey(key);
        configTranslations.put(translationKey, title);
        configTranslations.put(translationKey + ".tooltip", String.join(" ", comment));

        return builder.translation(translationKey)
                .comment(comment);
    }

    public ModConfigSpec build() {
        return builder.build();
    }
}
