package com.reveth.spctils.mixin.client.packetrecorder;

import com.reveth.spctils.features.debug.PacketRecorder;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    /**
     * Intercept incoming packets (S2C) - called when client receives a packet from server.
     */
    @Inject(method = "handlePacket", at = @At("HEAD"))
    private static void onHandlePacket(Packet<?> packet, net.minecraft.network.listener.PacketListener listener, CallbackInfo ci) {
        PacketRecorder.record(packet, true); // true = incoming (S2C)
    }

    /**
     * Intercept outgoing packets (C2S) - called when client sends a packet to server.
     * Targets the simplest overload: send(Packet)
     */
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"))
    private void onSend(Packet<?> packet, CallbackInfo ci) {
        PacketRecorder.record(packet, false); // false = outgoing (C2S)
    }
}
