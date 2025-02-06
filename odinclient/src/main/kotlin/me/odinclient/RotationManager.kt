package me.odinclient

import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.EntityEgg
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import javax.vecmath.Vector2f
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

object RotationManager {
    val mc: Minecraft = Minecraft.getMinecraft();
    var rotation: Rotation? = null
    var lastRotation: Rotation? = null
    var targetRotation: Rotation? = null
    var lastServerRotation: Rotation? = null
    private var rotationSpeed = 0f
    var modify: Boolean = false
    private var smoothed: Boolean = false
    private var movementFix = false
    private var strict = false

    fun setRotation(rotation: Rotation, rotationSpeed: Float, movementFix: Boolean, strict: Boolean) {
        this.targetRotation = rotation
        this.rotationSpeed = rotationSpeed
        this.movementFix = movementFix

        this.modify = true
        this.strict = strict
        smoothRotation()
    }

    fun setRotation(rotation: Rotation, rotationSpeed: Float, movementFix: Boolean) {
        this.targetRotation = rotation
        this.rotationSpeed = rotationSpeed
        this.movementFix = movementFix

        this.modify = true
        this.strict = false

        smoothRotation()
    }

    fun getAngles(entity: Entity): Rotation {
        val thePlayer = mc.thePlayer

        val diffX = entity.posX - thePlayer.posX
        val diffY = entity.posY + entity.eyeHeight * 0.9 - (thePlayer.posY + thePlayer.eyeHeight)
        val diffZ = entity.posZ - thePlayer.posZ
        val dist = sqrt(diffX * diffX + diffZ * diffZ).toDouble() // @on

        val yaw = (atan2(diffZ, diffX) * 180F / PI).toFloat() - 90.0F
        val pitch = -(atan2(diffY, dist) * 180F / PI).toFloat()
        return Rotation(yaw, pitch).fixedSensitivity()
    }


    fun getRotationDifference(rotation: Rotation): Double {
        return if (lastServerRotation == null) 0.0 else getRotationDifference(rotation, lastServerRotation!!)
    }

    fun getAngleDifference(a: Float, b: Float): Float {
        return ((((a - b) % 360f) + 540f) % 360f) - 180f
    }


    fun getRotationDifference(a: Rotation, b: Rotation): Double {
        return hypot(getAngleDifference(a.yaw, b.yaw).toDouble(), (a.pitch - b.pitch).toDouble())
    }

    fun getVectorForRotation(rotation: Vector2f): Vec3? {
        val yawCos = MathHelper.cos(-rotation.getX() * 0.017453292f - Math.PI.toFloat())
        val yawSin = MathHelper.sin(-rotation.getX() * 0.017453292f - Math.PI.toFloat())
        val pitchCos = -MathHelper.cos(-rotation.getY() * 0.017453292f)
        val pitchSin = MathHelper.sin(-rotation.getY() * 0.017453292f)
        return Vec3((yawSin * pitchCos).toDouble(), pitchSin.toDouble(), (yawCos * pitchCos).toDouble())
    }

    fun getVectorForRotation(rotation: Rotation): Vec3 {
        val yawCos = MathHelper.cos(-rotation.yaw * 0.017453292f - Math.PI.toFloat())
        val yawSin = MathHelper.sin(-rotation.yaw * 0.017453292f - Math.PI.toFloat())
        val pitchCos = -MathHelper.cos(-rotation.pitch * 0.017453292f)
        val pitchSin = MathHelper.sin(-rotation.pitch * 0.017453292f)
        return Vec3((yawSin * pitchCos).toDouble(), pitchSin.toDouble(), (yawCos * pitchCos).toDouble())
    }

    @SubscribeEvent
    fun onUpdate(event: TickEvent.PlayerTickEvent) {
        if (!modify || rotation == null || lastRotation == null || targetRotation == null) {
            targetRotation = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
            lastServerRotation = targetRotation
            lastRotation = lastServerRotation
            rotation = lastRotation
        }
        if (modify) {
            smoothRotation()
        }
    }

    fun onLook(): Rotation {
        var returns = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
        if (modify) {
            returns = Rotation(rotation!!.yaw, rotation!!.pitch)
            
            lastServerRotation = Rotation(rotation!!.yaw, rotation!!.pitch)

            if (abs(((rotation!!.yaw - mc.thePlayer.rotationYaw) % 360).toDouble()) < 1 && abs(
                    (rotation!!.pitch - mc.thePlayer.rotationPitch).toDouble()
                ) < 1
            ) {
                modify = false

                resetRotation(rotation)
            }

            lastRotation = rotation
        } else {
            lastRotation = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
        }

        targetRotation = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
        smoothed = false
        return returns;
    }

    init {
        this.rotation = Rotation(0f, 0f)
    }

    fun resetRotation(rotation: Rotation?): Rotation? {
        if (rotation == null) {
            return null
        }

        val yaw = rotation.yaw + getAngleDifference(rotation.yaw, mc.thePlayer.rotationYaw)
        val pitch = mc.thePlayer.rotationPitch
        return Rotation(yaw, pitch).fixedSensitivity()
    }

    private fun smoothRotation() {
        if (!smoothed) {
            if (lastRotation == null || targetRotation == null) return
            val lastYaw = lastRotation!!.yaw
            val lastPitch = lastRotation!!.pitch
            val targetYaw = targetRotation!!.yaw
            val targetPitch = targetRotation!!.pitch

            rotation = getSmoothRotation(
                Rotation(lastYaw, lastPitch), Rotation(targetYaw, targetPitch),
                rotationSpeed + Math.random()
            )
        }

        smoothed = true

        mc.entityRenderer.getMouseOver(1f)
    }

    fun getSmoothRotation(lastRotation: Rotation, targetRotation: Rotation, speed: Double): Rotation {
        var yaw = targetRotation.yaw
        var pitch = targetRotation.pitch
        val lastYaw = lastRotation.yaw
        val lastPitch = lastRotation.pitch

        if (speed != 0.0) {
            val rotationSpeed = speed.toFloat()

            val deltaYaw = getAngleDifference(yaw, lastYaw)
            val deltaPitch = getAngleDifference(pitch, lastPitch)

            val distance = hypot(deltaYaw, deltaPitch)

            val distributionYaw = abs(deltaYaw / distance)
            val distributionPitch = abs(deltaPitch / distance)

            val maxYaw = distributionYaw * rotationSpeed
            val maxPitch = distributionPitch * rotationSpeed

            val moveYaw = deltaYaw.coerceIn(-maxYaw, maxYaw)
            val movePitch = deltaPitch.coerceIn(-maxPitch, maxPitch)

            yaw = lastYaw + moveYaw
            pitch = lastPitch + movePitch
        }

        val randomise = Math.random() > 0.8

        for (i in 1..(2 + Math.random() * 2).toInt()) {
            if (randomise) {
                yaw += ((Math.random() - 0.5) / 100000000).toFloat()
                pitch -= (Math.random() / 200000000).toFloat()
            }

        }

        return Rotation(yaw, pitch)
    }
    /**
     * Returns the smallest angle difference possible with a specific sensitivity ("gcd")
     */
    fun getFixedAngleDelta(sensitivity: Float = mc.gameSettings.mouseSensitivity) =
        (sensitivity * 0.6f + 0.2f).pow(3) * 1.2f

    /**
     * Returns angle that is legitimately accomplishable with player's current sensitivity
     */
    fun getFixedSensitivityAngle(targetAngle: Float, startAngle: Float = 0f, gcd: Float = getFixedAngleDelta()) =
        startAngle + ((targetAngle - startAngle) / gcd).roundToInt() * gcd

    fun getDirection(rotationYaw: Float, moveForward: Double, moveStrafing: Double): Double {
        var shadowedRotationYaw = rotationYaw
        if (moveForward < 0f) shadowedRotationYaw += 180f

        var forward = 1f

        if (moveForward < 0f) forward = -0.5f
        else if (moveForward > 0f) forward = 0.5f

        if (moveStrafing > 0f) shadowedRotationYaw -= 90f * forward
        if (moveStrafing < 0f) shadowedRotationYaw += 90f * forward

        return Math.toRadians(shadowedRotationYaw.toDouble())
    }


    //get the rotation to block pos

    fun getRotations(blockPos: BlockPos, enumFacing: EnumFacing): Rotation {
        val d = blockPos.x.toDouble() + 0.5 - mc.thePlayer.posX + enumFacing.frontOffsetX.toDouble() * 0.25
        val d2 = blockPos.z.toDouble() + 0.5 - mc.thePlayer.posZ + enumFacing.frontOffsetZ.toDouble() * 0.25
        val d3 =
            mc.thePlayer.posY + mc.thePlayer.eyeHeight.toDouble() - blockPos.y - enumFacing.frontOffsetY.toDouble() * 0.25
        val d4 = MathHelper.sqrt_double(d * d + d2 * d2).toDouble()
        val f = (atan2(d2, d) * 180.0 / Math.PI).toFloat() - 90.0f
        val f2 = (atan2(d3, d4) * 180.0 / Math.PI).toFloat()
        return Rotation(MathHelper.wrapAngleTo180_float(f), f2)
    }

    fun getRotationsToBlock(block: Pair<BlockPos, EnumFacing>): Rotation? {
        val blockPos = block.first
        val enumFacing = block.second
        val x = blockPos.x + 0.5 - mc.thePlayer.posX + enumFacing.frontOffsetX.toDouble() / 2
        val z = blockPos.z + 0.5 - mc.thePlayer.posZ + enumFacing.frontOffsetZ.toDouble() / 2
        var y = blockPos.y + 0.5
        val dist = mc.thePlayer.getDistance(
            blockPos.x + 0.5 + enumFacing.frontOffsetX.toDouble() / 2,
            blockPos.y.toDouble(), blockPos.z + 0.5 + enumFacing.frontOffsetZ.toDouble() / 2
        )
        y += 0.5
        val d1 = mc.thePlayer.posY + mc.thePlayer.eyeHeight - y
        val d3 = MathHelper.sqrt_double(x * x + z * z).toDouble()
        var yaw = (Math.atan2(z, x) * 180.0 / Math.PI).toFloat() - 90
        val pitch = (Math.atan2(d1, d3) * 180.0 / Math.PI).toFloat()
        if (yaw < 0.0f) {
            yaw += 360f
        }
        return Rotation(yaw, pitch)
    }

    fun updateRotation(current: Float, intended: Float, factor: Float): Float {
        var var4 = MathHelper.wrapAngleTo180_float(intended - current)
        if (var4 > factor) {
            var4 = factor
        }
        if (var4 < -factor) {
            var4 = -factor
        }
        return current + var4
    }

    fun getDirectionToBlock(x: Double, y: Double, z: Double, enumfacing: EnumFacing): FloatArray? {
        val face = EntityEgg(mc.theWorld)
        face.posX = x + 0.5
        face.posY = y + 0.5
        face.posZ = z + 0.5
        face.posX += enumfacing.directionVec.x.toDouble() * 0.5
        face.posY += enumfacing.directionVec.y.toDouble() * 0.5
        face.posZ += enumfacing.directionVec.z.toDouble() * 0.5
        return getRotations(face.posX, face.posY, face.posZ)
    }

    fun getRotations(posX: Double, posY: Double, posZ: Double): FloatArray? {
        val to = Vec3(posX, posY, posZ)
        val from = Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ)
        val diff: Vec3 = to.subtract(from)
        val distance = Math.hypot(diff.xCoord, diff.zCoord)
        val yaw = Math.toDegrees(MathHelper.atan2(diff.zCoord, diff.xCoord)).toFloat() - 90.0f
        val pitch = (-Math.toDegrees(MathHelper.atan2(diff.yCoord, distance))).toFloat()
        return floatArrayOf(yaw, pitch)
    }
}