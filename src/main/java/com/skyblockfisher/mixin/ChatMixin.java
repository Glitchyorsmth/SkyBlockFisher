package com.skyblockfisher.mixin;

import com.skyblockfisher.FishingHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ChatMixin {

    @Inject(method = "onGameMessage", at = @At("TAIL"))
    private void skyblockfisher_onChat(GameMessageS2CPacket packet, CallbackInfo ci) {
        FishingHandler.getInstance().onChatMessage(packet.content().getString());
    }
}
