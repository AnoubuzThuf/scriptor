package com.ssblur.scriptor.word.descriptor.target.inventory

import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.helpers.targetable.InventoryTargetable
import com.ssblur.scriptor.helpers.targetable.Targetable
import com.ssblur.scriptor.word.descriptor.target.TargetDescriptor

class FirstFilledSlotDescriptor(registryKey: String): Descriptor(registryKey), TargetDescriptor {
  override fun modifyTargets(originalTargetables: List<Targetable>, owner: Targetable): List<Targetable> {
    originalTargetables.forEach {
      if (it is InventoryTargetable)
        (it as InventoryTargetable).useFirstFilledSlot()
    }
    return originalTargetables
  }

  override fun cost() = Cost(0.0, COSTTYPE.ADDITIVE)
  override fun replacesSubjectCost() = false
}
