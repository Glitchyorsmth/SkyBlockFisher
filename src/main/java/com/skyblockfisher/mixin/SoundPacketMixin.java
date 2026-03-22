package com.skyblockfisher.mixin;

import com.skyblockfisher.FishingHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class SoundPacketMixin {

    @Inject(method = "onPlaySound", at = @At("TAIL"))
    private void skyblockfisher_onPlaySound(PlaySoundS2CPacket packet, CallbackInfo ci) {
        try {
            FishingHandler.getInstance().onSoundPacket(packet);
        } catch (Exception e) {
            System.out.println("[SkyBlockFisher] Error in sound handler: " + e.getMessage());
        }
    }
}
