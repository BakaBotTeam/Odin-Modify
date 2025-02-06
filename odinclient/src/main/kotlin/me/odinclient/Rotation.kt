package me.odinclient

import me.odinclient.RotationManager.getFixedAngleDelta
import me.odinclient.RotationManager.getFixedSensitivityAngle
import me.odinclient.RotationManager.lastRotation
import me.odinmain.OdinMain.mc
import net.minecraft.entity.player.EntityPlayer

/**
 * Rotations
 */
data class Rotation(var yaw: Float, var pitch: Float) {
    /**
     * Set rotations to [player]
     */
    fun toPlayer(player: EntityPlayer) {
        if (yaw.isNaN() || pitch.isNaN())
            return

        fixedSensitivity(mc.gameSettings.mouseSensitivity)

        player.rotationYaw = yaw
        player.rotationPitch = pitch
        player.cameraPitch = pitch
        player.cameraYaw = yaw
    }

    /**
     * Patch gcd exploit in aim
     *
     * @see net.minecraft.client.renderer.EntityRenderer.updateCameraAndRender
     */
    fun fixedSensitivity(sensitivity: Float = mc.gameSettings.mouseSensitivity): Rotation {
        // Previous implementation essentially floored the subtraction.
        // This way it returns rotations closer to the original.

        // Only calculate GCD once
        val gcd = getFixedAngleDelta(sensitivity)

        yaw = getFixedSensitivityAngle(yaw, lastRotation!!.yaw, gcd)
        pitch = getFixedSensitivityAngle(pitch, lastRotation!!.pitch, gcd).coerceIn(-90f, 90f)

        return this
    }
}
