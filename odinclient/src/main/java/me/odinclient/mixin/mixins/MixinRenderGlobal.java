package me.odinclient.mixin.mixins;

import me.odinclient.features.impl.render.NoBreakEffect;
import me.odinmain.features.impl.render.RenderOptimizer;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {
    @Shadow @Final private Minecraft mc;

    @Inject(at = @At("HEAD"), method = "renderEntities", cancellable = true)
    public void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        RenderOptimizer.hookRenderEntities(renderViewEntity, camera, partialTicks, ci);
    }
    
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/audio/SoundHandler;playSound(Lnet/minecraft/client/audio/ISound;)V"), method = "playAuxSFX", cancellable = true)
    public void playAuxSFX(EntityPlayer player, int sfxType, BlockPos blockPosIn, int p_180439_4_, CallbackInfo ci) {
        if (NoBreakEffect.INSTANCE.getEnabled() && NoBreakEffect.INSTANCE.getNoBreakSound()) {
            ci.cancel();
            if (!NoBreakEffect.INSTANCE.getNoBreakParticles()) {
                Block block = Block.getBlockById(p_180439_4_ & 4095);
                mc.effectRenderer.addBlockDestroyEffects(blockPosIn, block.getStateFromMeta(p_180439_4_ >> 12 & 255));
            }
        }
    }
}
