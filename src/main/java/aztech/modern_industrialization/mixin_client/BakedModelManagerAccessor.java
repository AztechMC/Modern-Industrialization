package aztech.modern_industrialization.mixin_client;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(BakedModelManager.class)
public interface BakedModelManagerAccessor {
    @Accessor("models")
    Map<Identifier, BakedModel> getModels();
}
