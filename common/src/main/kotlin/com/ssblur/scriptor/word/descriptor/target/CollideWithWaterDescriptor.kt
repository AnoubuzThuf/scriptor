package com.ssblur.scriptor.word.descriptor.target

import com.ssblur.scriptor.api.word.Descriptor

class CollideWithWaterDescriptor(registryKey: String): Descriptor(registryKey) {
    override fun cost(): Cost = Cost.add(0.15)
}