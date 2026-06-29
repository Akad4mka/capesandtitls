package net.arm.mixin;

import net.arm.cape.CapeManager;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerSkin.class)
public class MixinPlayerSkin {

    @Shadow
    private ClientAsset.Texture cape;

    @Inject(method = "cape", at = @At("HEAD"), cancellable = true)
    private void injectCustomCape(CallbackInfoReturnable<ClientAsset.Texture> cir) {
        ClientAsset.Texture customCape = CapeManager.getActiveCapeTexture();
        if (customCape != null) {
            cir.setReturnValue(customCape);
            return;
        }

        if (this.cape != null) {
            if (this.cape.id().getPath().contains("empty_marker")) {
                cir.setReturnValue(null);
                return;
            }
            cir.setReturnValue(this.cape);
        }
    }
}