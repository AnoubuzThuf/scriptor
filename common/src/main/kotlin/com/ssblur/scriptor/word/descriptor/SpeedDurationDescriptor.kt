package com.ssblur.scriptor.word.descriptor

import com.ssblur.scriptor.word.descriptor.duration.SimpleDurationDescriptor

class SpeedDurationDescriptor(cost: Int, duration: Double, var speed: Double, registryKey: String):
  SimpleDurationDescriptor(cost, duration, registryKey), SpeedDescriptor {
  override fun speedModifier(): Double {
    return speed
  }
}
