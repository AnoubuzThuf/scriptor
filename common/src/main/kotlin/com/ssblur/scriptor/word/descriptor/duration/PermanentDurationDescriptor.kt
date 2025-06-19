package com.ssblur.scriptor.word.descriptor.duration

import com.ssblur.scriptor.api.word.Descriptor

open class PermanentDurationDescriptor: Descriptor(), DurationDescriptor {
//  Add Permanent duration effect to NPC's. 10x Very long duration for players

  var cost: Cost = Cost(400.0, COSTTYPE.ADDITIVE)
  var duplicates: Boolean = false

  override fun cost() = cost
  override fun durationModifier() = 1200.0
  override fun allowsDuplicates() = duplicates
}
