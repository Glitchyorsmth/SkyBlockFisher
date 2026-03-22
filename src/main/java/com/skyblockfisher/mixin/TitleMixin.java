package com.skyblockfisher.mixin;

import com.skyblockfisher.FishingHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class TitleMixin {

    @Inject(method = "onTitle", at = @At("TAIL"))
    private void skyblockfisher_onTitle(TitleS2CPacket packet, CallbackInfo ci) {
        try {
            String text = packet.text().getString();
            FishingHandler.getInstance().onTitleReceived(text);
        } catch (Exception e) {
            System.out.println("[SkyBlockFisher] Error in title handler: " + e.getMessage());
        }
    }

    @Inject(method = "onSubtitle", at = @At("TAIL"))
    private void skyblockfisher_onSubtitle(SubtitleS2CPacket packet, CallbackInfo ci) {
        try {
            String text = packet.text().getString();
            FishingHandler.getInstance().onTitleReceived(text);
        } catch (Exception e) {
            System.out.println("[SkyBlockFisher] Error in subtitle handler: " + e.getMessage());
        }
    }
}
