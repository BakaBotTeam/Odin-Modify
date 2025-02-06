package me.odinclient

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin

@IFMLLoadingPlugin.MCVersion("1.8.9")
class ASMPlugin : IFMLLoadingPlugin {

    override fun getASMTransformerClass(): Array<String> {
        return arrayOf("me.odinclient.mixin.transformer.PlayerControllerMPTransformer")
    }

    override fun getModContainerClass(): String? {
    return null
    }

    override fun getSetupClass(): String? {
    return null
    }

    override fun injectData(data: Map<String, Any>?) {
    }

    override fun getAccessTransformerClass(): String? {
    return null
    }
}