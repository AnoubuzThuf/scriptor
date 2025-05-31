package com.ssblur.scriptor.word.action

import com.ssblur.scriptor.ScriptorDamage.magic
import com.ssblur.scriptor.api.word.Action
import com.ssblur.scriptor.api.word.Word
import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.helpers.ItemTargetableHelper
import com.ssblur.scriptor.helpers.targetable.EntityTargetable
import com.ssblur.scriptor.helpers.targetable.ItemTargetable
import com.ssblur.scriptor.helpers.targetable.Targetable
import com.ssblur.scriptor.word.descriptor.power.StrengthDescriptor
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import kotlin.math.max

class HarmAction: Action() {
  override fun apply(caster: Targetable, targetable: Targetable, descriptors: Array<Descriptor>, words: Array<Word?>) {
    var strength = 1.0
    for (d in descriptors) {
      if (d is StrengthDescriptor) strength += d.strengthModifier()
    }

    strength = max(strength, 0.0)
    strength *= 2.0

    val itemTarget = ItemTargetableHelper.getTargetItemStack(
      targetable,
      false
    ) { !it.isEmpty && it.isDamageableItem }
    if (itemTarget.isDamageableItem) {
      itemTarget.damageValue += Math.round(strength).toInt()
      return
    }

    if (targetable is ItemTargetable && targetable.shouldTargetItem()) {
      val item: ItemStack = targetable.targetItem
      if (!item.isEmpty) {
        if (item.isDamageableItem) {
          item.damageValue += Math.round(strength).toInt()
          return
        }
      }
    }

    if (targetable is EntityTargetable) {
      val entity: Entity = targetable.targetEntity
      val source = if (caster is EntityTargetable) caster.targetEntity else entity
      if (entity is LivingEntity) if (entity.isInvertedHealAndHarm) entity.heal(strength.toFloat())
      else entity.hurt(magic(source, source), strength.toFloat())
    }
  }

  override fun cost() = Cost.add(3.0)
}
