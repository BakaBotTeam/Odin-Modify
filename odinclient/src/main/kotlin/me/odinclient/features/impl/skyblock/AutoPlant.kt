package me.odinclient.features.impl.skyblock

import me.odinclient.Rotation
import me.odinclient.RotationManager
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.smoothRotateTo
import net.minecraft.client.settings.KeyBinding
import net.minecraft.init.Blocks
import net.minecraft.item.ItemSeeds
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sqrt

object AutoPlant : Module(
    name = "Auto Plant",
    category = Category.SKYBLOCK,
    description = "Automatically plant on farming land.",
) {
    var targetBlock: BlockPos? = null
    var lastRotation: Rotation? = null
    
    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (mc.thePlayer == null || mc.theWorld == null) return
        if (targetBlock == null) {
            var nearestDistance = 4.5
            for (x in -(10)..10) {
            for (y in -(10)..10) {
            for (z in -(10)..10) {
                val bp = BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z)
                val block = mc.theWorld.getBlockState(bp).block
                val distance = mc.thePlayer.getDistanceSq(bp.x + .5, bp.y + .95, bp.z + .5)
                if (distance < nearestDistance && block == Blocks.farmland &&
                    mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y + 1, mc.thePlayer.posZ + z)).block == Blocks.air) {
                    val raytrace = mc.theWorld.rayTraceBlocks(mc.thePlayer.getPositionEyes(1f), Vec3(bp.x + .5, bp.y + .95, bp.z + .5))
                    if ((raytrace != null && raytrace.blockPos == bp) || raytrace == null) {
                        targetBlock = bp
                        nearestDistance = distance
                    }
                }
            }}}
            // calculate rotation
            if (targetBlock != null) {
                val rotation = getRotations(targetBlock!!.x + .5, targetBlock!!.y + .95, targetBlock!!.z + .5)
                lastRotation = Rotation(rotation!![0], rotation[1])
                RotationManager.setRotation(lastRotation!!, 180f, false)
            }
            return
        }
        RotationManager.setRotation(lastRotation ?: return, 180f, false)
        if (mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem) == null) return
        mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem), targetBlock!!, EnumFacing.UP, Vec3(targetBlock!!.x + .5, targetBlock!!.y + .94, targetBlock!!.z + .5))
        lastRotation = null
        targetBlock = null
    }

    fun getRotations(posX: Double, posY: Double, posZ: Double): FloatArray? {
        val to = Vec3(posX, posY, posZ)
        val from = Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ)
        val diff: Vec3 = to.subtract(from)
        val distance = hypot(diff.xCoord, diff.zCoord)
        val yaw = Math.toDegrees(MathHelper.atan2(diff.zCoord, diff.xCoord)).toFloat() - 90.0f
        val pitch = (-Math.toDegrees(MathHelper.atan2(diff.yCoord, distance))).toFloat()
        return floatArrayOf(yaw, pitch)
    }
}