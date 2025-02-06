package me.odinclient.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting

object NoBreakEffect : Module(
    name = "No Break Effect",
    category = Category.RENDER,
    description = "No description",
) {
    var noBlockHitParticles by BooleanSetting("No Hit Particles", default = true, false, "No Hit Particles")
    var noBreakParticles by BooleanSetting("No Break Particles", default = true, false, "No Break Particles")
    var noBreakSound by BooleanSetting("No Break Sound", default = true, false, "No Break Sound")
}
