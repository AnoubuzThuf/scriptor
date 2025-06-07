package com.ssblur.scriptor.word.descriptor.summon

import com.ssblur.scriptor.entity.SUMMON_PROPERTIES
import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.entity.SUMMON_BEHAVIOURS

open class SummonPropertyDescriptor(cost: Int, summonProperty: SUMMON_PROPERTIES): Descriptor() {
  var cost: Cost = Cost(cost.toDouble(), COSTTYPE.ADDITIVE)
  var summonProperty: SUMMON_PROPERTIES = summonProperty

  override fun cost() = cost
}
