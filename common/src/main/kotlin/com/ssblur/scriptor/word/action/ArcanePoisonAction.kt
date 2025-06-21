package com.ssblur.scriptor.word.action

import com.ssblur.scriptor.api.word.Action
import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.api.word.Word
import com.ssblur.scriptor.effect.ScriptorEffects.ARCANE_POISON
import com.ssblur.scriptor.effect.ScriptorEffects.FREEZE
import com.ssblur.scriptor.helpers.targetable.EntityTargetable
import com.ssblur.scriptor.helpers.targetable.Targetable
import com.ssblur.scriptor.word.action.potions.PotionAction
import com.ssblur.scriptor.word.descriptor.duration.DurationDescriptor
import com.ssblur.scriptor.word.descriptor.power.StrengthDescriptor
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import kotlin.math.floor
import kotlin.math.max
import net.minecraft.core.Holder
import net.minecraft.world.effect.MobEffects
import kotlin.math.min

/**
 * Damage over time status effect, dealing damage proportional to the strength and type of other damaging status effects
 * applied as part of the current spell. Useful for circumventing minecraft's damage cooldown invulnerability on mobs.
 * Supported status effects:
 * ignite, poison, wither, freeze
 * Each status effect can only contribute once, with diminishing returns. Scales better with more, weaker effects than
 * using a single strong effect.
 */
class ArcanePoisonAction: Action() {
    override fun apply(caster: Targetable, targetable: Targetable, descriptors: Array<Descriptor>, words: Array<Word?>) {
        // Damage/10 ticks = strength * 0.5
        var strength: Double = 1.0
        var strengthCapPercent: Float = 0f
        val strengthScale = 1.0/5.0
        val strengthCap = 10.0
        var duration: Double = 1.0
        var durationCapPercent: Float = 0f
        val durationScale = 30
        val durationCap = 600.0
        for (d in descriptors) {
            if (d is StrengthDescriptor) strength += d.strengthModifier()
            if (d is DurationDescriptor) duration += d.durationModifier()
        }
        val actionWords: List<Action> =  words.filter{ (it != null) and (it is Action)  }.map{ it as Action }
        val actionMobEffects: Set<Holder<MobEffect>> = actionWords.filter{ (it is PotionAction)  }.map{ it as PotionAction }.map{ it.mobEffect }.toSet()
        if (actionMobEffects.isNotEmpty() && targetable is EntityTargetable && targetable.targetEntity is LivingEntity) (targetable.targetEntity as LivingEntity).addEffect(
            MobEffectInstance(
                FREEZE, 7, 1
            )
        )

        /**
         * Let each complimentary_effect increase the damage cap and duration cap
         * Action - cost - cap contribution
         * Freeze - 12.0 - damage only
         * Wither - 12.0 - damage only
         * Poison - 8.0 - damage and duration
         * Inflame - 2.0 - damage and duration
         * Slow: - 6.0 - duration only
         * Weakness - 6.0 - duration only
         *
         * Max cap base cost - 48 + 2 = 50
         * Max duration = 120(65) / 4.0 = 30
         * Max duration cost = 120
         * Max strength = 30(120) / 5 = 10
         * Max strength cost = 120
         * Maxed spell cost = 50 + 120 + 120 = 290
         * Cheap maxed spell cost = 145
         */
//      Effect,  Duration cap percentage increase, Strength cap percentage increase
        val effect_map: List<Triple<Holder<MobEffect>, Float, Float>> = listOf(
            Triple(MobEffects.WITHER, 0f, 40f),
            Triple(FREEZE.ref(), 5f, 30f),
            Triple(MobEffects.POISON, 10f, 20f),
            Triple(MobEffects.WEAKNESS, 30f, 0f),
            Triple(MobEffects.MOVEMENT_SLOWDOWN, 30f, 0f),
        )
        for (effect_triple in effect_map) {
            if (effect_triple.first in actionMobEffects) {
                durationCapPercent += effect_triple.second
                strengthCapPercent += effect_triple.third
            }
        }
//        Fire is not applied through MobEffects
        if (actionWords.any{ (it is InflameAction) }) {
            durationCapPercent += 25f
            strengthCapPercent += 10f
        }

        strength = max(strength, 1.0)
        duration = max(duration, 1.0)
        strength = min(strength*strengthScale, (min(strengthCapPercent, 100f)*strengthCap/100f))
        duration = min(duration*durationScale, min(durationCapPercent, 100f)*durationCap/100f)

        duration = duration.coerceAtLeast(1.0)

        if (targetable is EntityTargetable && targetable.targetEntity is LivingEntity) (targetable.targetEntity as LivingEntity).addEffect(
            MobEffectInstance(
                ARCANE_POISON, Math.round(duration).toInt(), floor(strength).toInt()
            )
        )
    }

    override fun cost() = Cost(2.0, COSTTYPE.ADDITIVE)
}