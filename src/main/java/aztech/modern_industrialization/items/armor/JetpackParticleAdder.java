package aztech.modern_industrialization.items.armor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class JetpackParticleAdder {
    public static void addJetpackParticles(MinecraftClient client) {
        if(client.world != null) {
            for (PlayerEntity player : client.world.getPlayers()) {
                ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
                if (chest.getItem() instanceof JetpackItem) {
                    JetpackItem jetpack = (JetpackItem) chest.getItem();
                    if (jetpack.showParticles(chest) && jetpack.getAmount(chest) > 0) {
                        Random r = ThreadLocalRandom.current();
                        for(int i = 0; i < 20; ++i) {
                            client.world.addParticle(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), r.nextFloat() - 0.5, -5, r.nextFloat() - 0.5);
                        }
                    }
                }
            }
        }
    }
}
