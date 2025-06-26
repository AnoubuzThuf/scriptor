package com.ssblur.scriptor.word.descriptor.meta

import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.entity.SUMMON_BEHAVIOURS

open class DescriptorMultiplier(val multiplier: Int, registryKey: String): Descriptor(registryKey) {
    override fun cost(): Cost {
        return Cost(0.0, COSTTYPE.ADDITIVE)
    }
}
