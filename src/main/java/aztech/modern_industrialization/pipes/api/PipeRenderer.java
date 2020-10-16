package aztech.modern_industrialization.pipes.api;

import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.nbt.CompoundTag;

import java.util.Collection;
import java.util.function.Function;

// TODO: Refactor to split api and impl correctly, and provide building blocks in API if necessary
public interface PipeRenderer {
    /**
     * Draw the connections for a logical slot.
     * @param ctx Render context.
     * @param logicalSlot The logical slot, so 0 for center, 1 for lower and 2 for upper.
     * @param connections For every logical slot, then for every direction, the connection type or null for no connection.
     */
    void draw(RenderContext ctx, int logicalSlot, PipeEndpointType[][] connections, CompoundTag customData);

    interface Factory {
        Collection<SpriteIdentifier> getSpriteDependencies();
        PipeRenderer create(Function<SpriteIdentifier, Sprite> textureGetter);
    }
}
