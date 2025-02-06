package me.odinclient.mixin.mixins;

import me.odinclient.features.impl.render.NoBreakEffect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EffectRenderer.class)
public class MixinEffectRender {
    @Inject(method = "addBlockDestroyEffects", at = @At("HEAD"), cancellable = true)
    public void addBlockDestroyEffects(BlockPos pos, IBlockState state, CallbackInfo ci) {
        if (NoBreakEffect.INSTANCE.getEnabled() && NoBreakEffect.INSTANCE.getNoBreakParticles()) {
            ci.cancel();
        }
    }
    
    @Inject(method = "addBlockHitEffects(Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;)V", at = @At("HEAD"), cancellable = true)
    public void addBlockHitEffects(BlockPos pos, EnumFacing side, CallbackInfo ci) {
        if (NoBreakEffect.INSTANCE.getEnabled() && NoBreakEffect.INSTANCE.getNoBlockHitParticles()) {
            ci.cancel();
        }
    }
}
