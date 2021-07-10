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
package aztech.modern_industrialization.compat.kubejs;

import dev.latvian.kubejs.event.EventJS;
import dev.latvian.kubejs.script.ScriptType;
import java.util.HashSet;
import java.util.Set;

public class RemoveItemsEntrypoint {
    public static Set<String> getItemsToRemove() {
        return MIReiEvent.gatherItemsToRemove();
    }

    public static class MIReiEvent extends EventJS {
        private final Set<String> removeSet = new HashSet<>();

        public void remove(String item) {
            removeSet.add(item);
        }

        private static Set<String> gatherItemsToRemove() {
            MIReiEvent event = new MIReiEvent();
            event.post(ScriptType.CLIENT, "mi_rei");
            return event.removeSet;
        }
    }
}
