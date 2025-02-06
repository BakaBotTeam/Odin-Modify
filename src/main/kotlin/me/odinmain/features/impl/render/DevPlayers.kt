package me.odinmain.features.impl.render


import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.render.ClickGUIModule.devSize
import me.odinmain.utils.render.Color
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.model.ModelBase
import net.minecraft.client.model.ModelRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.cos
import kotlin.math.sin

object DevPlayers {
    private var devs: HashMap<String, DevPlayer> = HashMap()
    val isDev get() = true

    data class DevPlayer(val xScale: Float = 1f, val yScale: Float = 1f, val zScale: Float = 1f,
                         val wings: Boolean = false, val wingsColor: Color = Color(255, 255, 255), var capeLocation: ResourceLocation? = null)

    fun preRenderCallbackScaleHook(entityLivingBaseIn: AbstractClientPlayer) {
        if (!devSize && entityLivingBaseIn.name == mc.thePlayer.name) return
        if (ClickGUIModule.devSizeY < 0) GlStateManager.translate(0f, ClickGUIModule.devSizeY * 2, 0f)
        GlStateManager.scale(ClickGUIModule.devSizeX, ClickGUIModule.devSizeY, ClickGUIModule.devSizeZ)
    }

    @SubscribeEvent
    fun onRenderPlayer(event: RenderPlayerEvent.Post) {
        if (!ClickGUIModule.devWings) return
        DragonWings.renderWings(event.entityPlayer, event.partialRenderTick )
    }

    object DragonWings : ModelBase() {

        private val dragonWingTextureLocation = ResourceLocation("textures/entity/enderdragon/dragon.png")
        private val wing: ModelRenderer
        private val wingTip: ModelRenderer

        init {
            textureWidth = 256
            textureHeight = 256
            setTextureOffset("wing.skin", -56, 88)
            setTextureOffset("wingtip.skin", -56, 144)
            setTextureOffset("wing.bone", 112, 88)
            setTextureOffset("wingtip.bone", 112, 136)

            wing = ModelRenderer(this, "wing")
            wing.setRotationPoint(-12.0f, 5.0f, 2.0f)
            wing.addBox("bone", -56.0f, -4.0f, -4.0f, 56, 8, 8)
            wing.addBox("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56)
            wingTip = ModelRenderer(this, "wingtip")
            wingTip.setRotationPoint(-56.0f, 0.0f, 0.0f)
            wingTip.addBox("bone", -56.0f, -2.0f, -2.0f, 56, 4, 4)
            wingTip.addBox("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56)
            wing.addChild(wingTip)
        }

        fun renderWings(player: EntityPlayer, partialTicks: Float) {
            val rotation = this.interpolate(player.prevRenderYawOffset, player.renderYawOffset, partialTicks)

            GlStateManager.pushMatrix()
            val x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks
            val y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks
            val z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks
            if (ClickGUIModule.devSizeY < 0) GlStateManager.translate(0f, ClickGUIModule.devSizeY * -2, 0f)

            GlStateManager.translate(-mc.renderManager.viewerPosX + x, -mc.renderManager.viewerPosY + y, -mc.renderManager.viewerPosZ + z)
            GlStateManager.scale(-0.2, -0.2, 0.2)
            GlStateManager.scale(ClickGUIModule.devSizeX, ClickGUIModule.devSizeY, ClickGUIModule.devSizeZ)
            GlStateManager.rotate(180 + rotation, 0f, 1f, 0f)
            GlStateManager.translate(0.0, -(1.25 / 0.2f), 0.0)
            GlStateManager.translate(0.0, 0.0, 0.25)

            if (player.isSneaking) {
                GlStateManager.rotate(45f, 1f, 0f, 0f)
                GlStateManager.translate(0.0, 1.0, -0.5)
            }

            GlStateManager.color(ClickGUIModule.devWingsColor.r.toFloat()/255, ClickGUIModule.devWingsColor.g.toFloat()/255, ClickGUIModule.devWingsColor.b.toFloat()/255, 1f)
            mc.textureManager.bindTexture(dragonWingTextureLocation)

            for (j in 0..1) {
                GlStateManager.enableCull()
                GlStateManager.rotate(20f, 0f, 1f, 0f)
                val f11 = System.currentTimeMillis() % 1000 / 1000f * Math.PI.toFloat() * 2.0f
                wing.rotateAngleX = Math.toRadians(-80.0).toFloat() - cos(f11) * 0.2f
                wing.rotateAngleY = Math.toRadians(20.0).toFloat() + sin(f11) * 0.4f
                wing.rotateAngleZ = Math.toRadians(20.0).toFloat()
                wingTip.rotateAngleZ = -(sin((f11 + 2.0f)) + 0.5).toFloat() * 0.75f
                wing.render(0.0625f)
                GlStateManager.scale(-1.0f, 1.0f, 1.0f)
                if (j == 0) {
                    GlStateManager.rotate(20f, 0f, 1f, 0f)
                    GlStateManager.cullFace(1028)
                }
            }

            GlStateManager.cullFace(1029)
            GlStateManager.disableCull()
            GlStateManager.color(1f, 1f, 1f, 1f)
            GlStateManager.popMatrix()
        }

        private fun interpolate(yaw1: Float, yaw2: Float, percent: Float): Float {
            var f = (yaw1 + (yaw2 - yaw1) * percent) % 360
            if (f < 0) {
                f += 360f
            }
            return f
        }
    }
}