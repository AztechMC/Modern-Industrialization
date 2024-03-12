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
package aztech.modern_industrialization.compat.argonauts;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModList;

public interface ArgonautsFacade {
    ArgonautsFacade INSTANCE = getInstance();

    private static ArgonautsFacade getInstance() {
        if (ModList.get().isLoaded("argonauts")) {
            try {
                return Class.forName("aztech.modern_industrialization.compat.argonauts.ArgonautsFacadeImpl")
                        .asSubclass(ArgonautsFacade.class).getConstructor().newInstance();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

        return (ms, uuid) -> List.of();
    }

    Collection<UUID> getOtherPlayersInGuild(MinecraftServer server, UUID playerUuid);
}
