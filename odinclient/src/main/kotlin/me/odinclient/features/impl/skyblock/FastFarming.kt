package me.odinclient.features.impl.skyblock

import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import net.minecraft.block.Block
import net.minecraft.block.BlockCocoa
import net.minecraft.block.BlockCrops
import net.minecraft.block.BlockNetherWart
import net.minecraft.block.BlockStem
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object FastFarming : Module(
    name = "Fast Farming",
    category = Category.SKYBLOCK,
    description = "(BANNING RISK) Automatically breaks.",
) {
    var handlePacket = true
    
    @SubscribeEvent
    fun onSendPacket(event: PacketEvent.Send) {
        if (handlePacket && event.packet is C07PacketPlayerDigging && (event.packet as C07PacketPlayerDigging).status == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
            val packet = event.packet as C07PacketPlayerDigging
            val blockPos = packet.position.offset(packet.facing.opposite)
            if (mc.theWorld.getBlockState(blockPos).block.isCrop()) {
                handlePacket = false
                val newPacket = C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, packet.facing)
                val stopPacket = C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, packet.facing)
                mc.netHandler.addToSendQueue(newPacket)
                mc.netHandler.addToSendQueue(stopPacket)
                handlePacket = true
                mc.theWorld.setBlockToAir(blockPos)
            }
        }
    }
    
    private fun Block.isCrop(): Boolean {
        return this is BlockCrops || this is BlockStem || this is BlockCocoa || this is BlockNetherWart
    }
}