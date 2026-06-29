package net.arm.mixin;

import net.arm.cape.CapeManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(AbstractClientPlayer.class)
public class MixinAbstractClientPlayer {

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void injectCustomCapeForAnyPlayer(CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        UUID uuid = player.getUUID();

        PlayerSkin originalSkin = cir.getReturnValue();

        if (originalSkin != null) {
            ClientAsset.Texture customCape = CapeManager.getCapeTextureForPlayer(uuid);

            if (customCape != null) {
                PlayerSkin updatedSkin = new PlayerSkin(
                        originalSkin.body(),
                        customCape,
                        originalSkin.elytra(),
                        originalSkin.model(),
                        originalSkin.secure()
                );

                // Возвращаем измененный PlayerSkin
                cir.setReturnValue(updatedSkin);
            }
        }
    }
}