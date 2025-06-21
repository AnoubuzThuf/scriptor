package com.ssblur.scriptor.word.action

import com.ssblur.scriptor.api.word.Action
import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.api.word.Word
import com.ssblur.scriptor.data.saved_data.VoodooSpellSavedData
import com.ssblur.scriptor.effect.ScriptorEffects.VOODOO_EFFECT
import com.ssblur.scriptor.helpers.targetable.EntityTargetable
import com.ssblur.scriptor.helpers.targetable.Targetable
import com.ssblur.scriptor.word.descriptor.duration.DurationDescriptor
import com.ssblur.scriptor.word.descriptor.duration.PermanentDurationDescriptor
import com.ssblur.scriptor.word.descriptor.power.StrengthDescriptor
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import kotlin.math.floor
import kotlin.math.sqrt

class VoodooHarmOtherAction: Action() {
    override fun apply(caster: Targetable, targetable: Targetable, descriptors: Array<Descriptor>, words: Array<Word?>) {
        if (caster is EntityTargetable && caster.targetEntity is LivingEntity) {
            val casterEntity = caster.targetEntity as LivingEntity
            var strength: Double = 1.0
            var duration: Double = 5.0
            for (d in descriptors) {
                if (d is StrengthDescriptor) strength += d.strengthModifier()
                if (d is DurationDescriptor) duration += d.durationModifier()
            }

            strength = sqrt(strength) * 10
            duration = duration * 20

            duration = duration.coerceAtLeast(1.0)

            if (targetable is EntityTargetable && targetable.targetEntity is LivingEntity && targetable.targetEntity !is Player) {
                if (descriptors.any {it is PermanentDurationDescriptor }) {
                    duration = -1.0
                }
            }

            if (targetable is EntityTargetable && targetable.targetEntity is LivingEntity) {
                val benefitor = casterEntity
                val victim = targetable.targetEntity as LivingEntity
                benefitor.addEffect(
                    MobEffectInstance(
                        VOODOO_EFFECT, Math.round(duration).toInt(), floor(strength).toInt()
                    ))
                val voodooVictimIdHolder = VoodooSpellSavedData.computeIfAbsent(benefitor)
                if (voodooVictimIdHolder != null) {
                    voodooVictimIdHolder.voodooSubjectUuid = victim.uuid
                    voodooVictimIdHolder.setDirty()
                }
            }
        }
    }

    override fun cost() = Cost(2.0, COSTTYPE.ADDITIVE)
}