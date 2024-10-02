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

import aztech.modern_industrialization.machines.gui.GuiComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;

public sealed class ComponentStorage<C> permits ComponentStorage.GuiServer, ComponentStorage.Server {
    protected final List<C> components = new ArrayList<>();

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

    public final void forEach(Consumer<C> action) {
        components.forEach(action);
    }

    public final void forEachIndexed(BiConsumer<Integer, C> action) {
        for (int i = 0; i < components.size(); i++) {
            action.accept(i, components.get(i));
        }
    }

    public final <T> Optional<T> get(Class<T> clazz) {
        for (C component : components) {
            if (clazz.isInstance(component)) {
                return Optional.of((T) component);
            }
        }
        return Optional.empty();
    }

    public final <T> List<T> tryGet(Class<T> clazz) {
        List<T> components = new ArrayList<>();
        for (C component : this.components) {
            if (clazz.isInstance(component)) {
                components.add((T) component);
            }
        }
        return components;
    }

    public final <T> void forType(Class<T> clazz, Consumer<? super T> action) {
        List<T> component = tryGet(clazz);
        for (T c : component) {
            action.accept(c);
        }
    }

    public final <T, R> R mapOrDefault(Class<T> clazz, Function<? super T, ? extends R> action, R defaultValue) {
        List<T> components = tryGet(clazz);
        if (components.isEmpty()) {
            return defaultValue;
        } else if (components.size() == 1) {
            return action.apply(components.get(0));
        } else {
            throw new RuntimeException("Multiple components of type " + clazz.getName() + " found");
        }
    }

    public final <R> R findOrDefault(Function<C, Optional<? extends R>> action, R defaultValue) {
        for (C component : components) {
            Optional<? extends R> result = action.apply(component);
            if (result.isPresent()) {
                return result.get();
            }
        }
        return defaultValue;
    }

    public final <T, R> R findOrDefault(Class<T> clazz, Function<? super T, Optional<? extends R>> action, R defaultValue) {
        return findOrDefault(component -> clazz.isInstance(component) ? action.apply((T) component) : Optional.empty(), defaultValue);
    }

    public static final class GuiServer extends ComponentStorage<GuiComponent.Server> {
        /**
         * @throws RuntimeException if the component doesn't exist.
         */
        public <S extends GuiComponent.Server> S get(ResourceLocation componentId) {
            for (GuiComponent.Server component : components) {
                if (component.getId().equals(componentId)) {
                    return (S) component;
                }
            }
            throw new RuntimeException("Couldn't find component " + componentId);
        }
    }

    public static final class Server extends ComponentStorage<IComponent> {
    }
}
