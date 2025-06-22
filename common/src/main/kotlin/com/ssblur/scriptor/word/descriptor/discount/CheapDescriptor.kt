package com.ssblur.scriptor.word.descriptor.discount

import com.ssblur.scriptor.api.word.Descriptor

class CheapDescriptor(registryKey: String): Descriptor(registryKey) {
  override fun cost() = Cost(0.5, COSTTYPE.MULTIPLICATIVE)
}
