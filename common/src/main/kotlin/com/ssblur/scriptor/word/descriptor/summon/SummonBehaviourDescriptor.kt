package com.ssblur.scriptor.word.descriptor.summon

import com.ssblur.scriptor.entity.SUMMON_BEHAVIOURS
import com.ssblur.scriptor.api.word.Descriptor

open class SummonBehaviourDescriptor(cost: Int, behaviour: SUMMON_BEHAVIOURS): Descriptor() {
  var cost: Cost = Cost(cost.toDouble(), COSTTYPE.ADDITIVE)
  var behaviour: SUMMON_BEHAVIOURS = behaviour
  override fun cost() = cost
}
