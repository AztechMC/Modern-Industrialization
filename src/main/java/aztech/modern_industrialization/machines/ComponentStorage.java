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

package aztech.modern_industrialization.machines;

import aztech.modern_industrialization.machines.gui.GuiComponentServer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

public sealed class ComponentStorage<C> implements Iterable<C> permits ComponentStorage.GuiServer, ComponentStorage.Server {
    protected final List<C> components = new ArrayList<>();

    @Override
    public Iterator<C> iterator() {
        return components.iterator();
    }

    @SafeVarargs
    public final void register(C... components) {
        Collections.addAll(this.components, components);
    }

    @SafeVarargs
    public final void unregister(C... components) {
        for (C component : components) {
            this.components.remove(component);
        }
    }

    public final int size() {
        return components.size();
    }

    public final C get(int index) {
        return components.get(index);
    }

    public final void forEachIndexed(BiConsumer<Integer, C> action) {
        for (int i = 0; i < components.size(); i++) {
            action.accept(i, components.get(i));
        }
    }

    @Nullable
    public final <T> T getNullable(Class<T> clazz) {
        for (C component : components) {
            if (clazz.isInstance(component)) {
                return (T) component;
            }
        }
        return null;
    }

    public final <T> T getOrThrow(Class<T> clazz) {
        T ret = getNullable(clazz);
        if (ret == null) {
            throw new RuntimeException("Component not found: " + clazz);
        }
        return ret;
    }

    public final <T> List<T> getAll(Class<T> clazz) {
        List<T> components = new ArrayList<>();
        for (C component : this.components) {
            if (clazz.isInstance(component)) {
                components.add((T) component);
            }
        }
        return components;
    }

    public final <T> void forType(Class<T> clazz, Consumer<? super T> action) {
        List<T> component = getAll(clazz);
        for (T c : component) {
            action.accept(c);
        }
    }

    public final <T, R> R mapOrDefault(Class<T> clazz, Function<? super T, ? extends R> action, R defaultValue) {
        List<T> components = getAll(clazz);
        if (components.isEmpty()) {
            return defaultValue;
        } else if (components.size() == 1) {
            return action.apply(components.get(0));
        } else {
            throw new RuntimeException("Multiple components of type " + clazz.getName() + " found");
        }
    }

    public static final class GuiServer extends ComponentStorage<GuiComponentServer<?, ?>> {}

    public static final class Server extends ComponentStorage<IComponent> {}
}
