package aztech.modern_industrialization.client;

import aztech.modern_industrialization.MI;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.io.IOException;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jspecify.annotations.Nullable;

public class MIShaders {
    @Nullable
    private static ShaderInstance CUTOUT_HIGHLIGHT_INSTANCE;

    private static ShaderInstance cutoutHighlight() {
        return CUTOUT_HIGHLIGHT_INSTANCE;
    }

    public static final RenderStateShard.ShaderStateShard CUTOUT_HIGHLIGHT = new RenderStateShard.ShaderStateShard(MIShaders::cutoutHighlight);

    public static void init(IEventBus bus) {
        bus.addListener(MIShaders::registerShaders);
    }

    private static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), MI.id("cutout_highlight"), DefaultVertexFormat.BLOCK), (shader) -> CUTOUT_HIGHLIGHT_INSTANCE = shader);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
