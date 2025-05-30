package com.ssblur.scriptor.word.descriptor.power

import com.ssblur.scriptor.ScriptorDamage.sacrifice
import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.helpers.targetable.EntityTargetable
import com.ssblur.scriptor.helpers.targetable.Targetable
import com.ssblur.scriptor.word.descriptor.CastDescriptor
import net.minecraft.world.entity.LivingEntity

class BloodPowerDescriptor: Descriptor(), CastDescriptor, StrengthDescriptor {
  override fun cannotCast(caster: Targetable?): Boolean {
    if (caster is EntityTargetable && caster.targetEntity is LivingEntity) {
      val living = caster.targetEntity as LivingEntity
      living.invulnerableTime = 0
      living.hurt(sacrifice(living), 1.0f)
      living.invulnerableTime = 0
      return !living.isAlive()
    }
    return true
  }

  override fun cost() = Cost(0.0, COSTTYPE.ADDITIVE_POST)
  override fun strengthModifier() = 2.0
  override fun allowsDuplicates() = true
}
