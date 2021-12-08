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
package aztech.modern_industrialization.machines.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * A machine recipe type that allows adding proxies
 */
public abstract class ProxyableMachineRecipeType extends MachineRecipeType {
    public ProxyableMachineRecipeType(Identifier id) {
        super(id);
    }

    private long lastUpdate = 0;
    private static final long UPDATE_INTERVAL = 20 * 1000;
    protected List<MachineRecipe> recipeList = new ArrayList<>();

    protected abstract void fillRecipeList(World world);

    @Override
    public Collection<MachineRecipe> getRecipes(World world) {
        long time = System.currentTimeMillis();
        if (time - lastUpdate > UPDATE_INTERVAL) {
            lastUpdate = time;
            recipeList.clear();
            fillRecipeList(world);
        }
        return recipeList;
    }
}
