package com.ssblur.scriptor.api.word

import com.ssblur.scriptor.effect.ScriptorEffects.UNARMOURED_OATH_EFFECT
import com.ssblur.scriptor.helpers.targetable.EntityTargetable
import com.ssblur.scriptor.helpers.targetable.ItemTargetable
import com.ssblur.scriptor.helpers.targetable.Targetable
import net.minecraft.world.entity.LivingEntity

abstract class Action(val registryKey: String): Word() {
  /**
   * Applies the effects of this spell.
   * This step should factor in any Descriptors on this spell.
   * @param caster The Entity which cast this spell
   * @param targetable A Targetable which describes the target of this spell (position, entity, item, etc.)
   * @param descriptors A list of all Descriptors which this spell contains
   */
  abstract fun apply(caster: Targetable, targetable: Targetable, descriptors: Array<Descriptor>, words: Array<Word?>)

  fun getMetaPowerMultiplier(caster: Targetable, targetable: Targetable): Double {
    var multiplier = 1.0
//    Handle oaths
    return multiplier
  }

  fun getMetaDurationMultiplier(caster: Targetable, targetable: Targetable): Double {
    var multiplier = 1.0
//    Handle oaths
    if (caster is EntityTargetable && targetable is EntityTargetable) {
      if (caster.targetEntity == targetable.targetEntity && caster.targetEntity is LivingEntity) {
        val entity = caster.targetEntity as LivingEntity
        if (entity.activeEffects.any {it.effect.value() == UNARMOURED_OATH_EFFECT.value }) {
          multiplier = multiplier * 2.0
        }
      }
    }
    return multiplier
  }
}
