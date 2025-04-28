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
package aztech.modern_industrialization.items;

import aztech.modern_industrialization.nuclear.NuclearOrder;
import org.jetbrains.annotations.NotNull;

public final class SortOrder implements Comparable<SortOrder> {
    public static final SortOrder GUIDE_BOOK = new SortOrder();
    public static final SortOrder FORGE_HAMMER = new SortOrder();
    public static final SortOrderGroup.Counted ITEMS_ORDERED = new SortOrderGroup.Counted(new SortOrder());
    public static final SortOrderGroup.Parametrized<NuclearOrder> NUCLEAR = new SortOrderGroup.Parametrized<>(new SortOrder());
    public static final SortOrder CABLES = new SortOrder();
    public static final SortOrder PIPES = new SortOrder();
    public static final SortOrder TANKS = new SortOrder();
    public static final SortOrder BARRELS = new SortOrder();
    public static final SortOrder MACHINES = new SortOrder();
    public static final SortOrder CASINGS = new SortOrder();
    public static final SortOrder COILS = new SortOrder();
    public static final SortOrder BLOCKS_OTHERS = new SortOrder();
    public static final SortOrder ORES = new SortOrder();
    public static final SortOrder RAW_ORE_BLOCKS = new SortOrder();
    public static final SortOrder STORAGE_BLOCKS = new SortOrder();
    public static final SortOrder MATERIALS = new SortOrder();
    public static final SortOrder BUCKETS = new SortOrder();

    private static int nextGlobalOrder = 0;

    private final Comparable[] objects;

    // Root constructor
    private SortOrder() {
        this(nextGlobalOrder++);
    }

    private SortOrder(Comparable... objects) {
        this.objects = objects;
    }

    public SortOrder and(Comparable anotherObject) {
        Comparable[] newArray = new Comparable[objects.length + 1];
        System.arraycopy(objects, 0, newArray, 0, objects.length);
        newArray[objects.length] = anotherObject;
        return new SortOrder(newArray);
    }

    @Override
    public int compareTo(@NotNull SortOrder o) {
        int i;
        for (i = 0; i < Math.min(o.objects.length, objects.length); ++i) {
            @SuppressWarnings("unchecked")
            int comp = objects[i].compareTo(o.objects[i]);
            if (comp != 0) {
                return comp;
            }
        }

        if (i < objects.length) {
            return 1;
        } else if (i < o.objects.length) {
            return -1;
        } else {
            return 0;
        }
    }
}
