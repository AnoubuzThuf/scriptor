package com.ssblur.scriptor.word.descriptor.discount

import com.ssblur.scriptor.ScriptorDamage.sacrifice
import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.helpers.targetable.EntityTargetable
import com.ssblur.scriptor.helpers.targetable.Targetable
import com.ssblur.scriptor.word.descriptor.CastDescriptor
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player

class ExperienceCostDescriptor: Descriptor(), CastDescriptor {
//  Each use costs 10 levels
  override fun cost() = Cost(-200.0, COSTTYPE.ADDITIVE_POST)

  override fun cannotCast(caster: Targetable?): Boolean {
    if (caster is EntityTargetable && caster.targetEntity is Player) {
      val living = caster.targetEntity as Player
      if (living.experienceLevel >= 1) {
        living.giveExperienceLevels(-1)
        return false
      }
    }
    return true
  }

  override fun allowsDuplicates() = true
}