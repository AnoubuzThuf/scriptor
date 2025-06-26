package com.ssblur.scriptor.word.action.oaths

import com.ssblur.scriptor.api.word.Action
import com.ssblur.scriptor.api.word.Word
import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.helpers.targetable.EntityTargetable
import com.ssblur.scriptor.helpers.targetable.Targetable
import com.ssblur.scriptor.word.descriptor.duration.DurationDescriptor
import com.ssblur.scriptor.word.descriptor.duration.PermanentDurationDescriptor
import com.ssblur.scriptor.word.descriptor.power.StrengthDescriptor
import net.minecraft.core.Holder
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import kotlin.math.floor
import kotlin.math.max

open class OathAction(
  var mobEffect: Holder<MobEffect>,
  var strengthScale: Double,
  var cost: Cost,
  val strengthCap: Double? = null,
  registryKey: String): Action(registryKey) {
  override fun apply(caster: Targetable, targetable: Targetable, descriptors: Array<Descriptor>, words: Array<Word?>) {
//    Oaths can only be cast 'voluntarily', so cast only if caster is targetable.
    if (caster is EntityTargetable && targetable is EntityTargetable) {
      if (caster.targetEntity == targetable.targetEntity) {
        var strength = 0.0
        for (d in descriptors) {
          if (d is StrengthDescriptor) strength += d.strengthModifier()
        }

        strength = max(strength, 0.0)
        strength *= strengthScale

        if (strengthCap != null && strength > strengthCap) {
          strength = strengthCap
        }

        // Maybe add poison-tipped enchant?
        if (targetable.targetEntity is LivingEntity) (targetable.targetEntity as LivingEntity).addEffect(
          MobEffectInstance(
            mobEffect, -1, floor(strength).toInt()
          )
        )
      }
    }
  }

  override fun cost() = this.cost
}
