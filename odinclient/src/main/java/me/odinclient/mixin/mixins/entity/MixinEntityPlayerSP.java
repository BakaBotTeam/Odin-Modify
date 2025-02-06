package me.odinclient.mixin.mixins.entity;

import me.odinclient.Rotation;
import me.odinclient.RotationManager;
import me.odinmain.events.impl.MessageSentEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.odinmain.utils.Utils.postAndCatch;

@Mixin(value = EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP {
    @Shadow protected Minecraft mc;
    private float renderYaw;
    private float renderPitch;

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (postAndCatch(new MessageSentEvent(message))) ci.cancel();
    }
    
    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"))
    private void onPreUpdateWalkingPlayer(CallbackInfo ci) {
        Rotation rotation = RotationManager.INSTANCE.onLook();
        this.renderYaw = this.mc.thePlayer.rotationYaw;
        this.renderPitch = this.mc.thePlayer.rotationPitch;
        this.mc.thePlayer.rotationYaw = rotation.getYaw();
        this.mc.thePlayer.rotationPitch = rotation.getPitch();
    }
    
    @Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"))
    private void onPostUpdateWalkingPlayer(CallbackInfo ci) {
        this.mc.thePlayer.rotationYaw = this.renderYaw;
        this.mc.thePlayer.rotationPitch = this.renderPitch;
    }
}
