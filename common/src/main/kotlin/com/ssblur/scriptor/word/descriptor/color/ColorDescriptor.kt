package com.ssblur.scriptor.word.descriptor.color

import com.ssblur.scriptor.api.word.Descriptor

open class ColorDescriptor(var color: Int, registryKey: String): Descriptor(registryKey) {
  override fun cost() = Cost(0.0, COSTTYPE.ADDITIVE)
}
