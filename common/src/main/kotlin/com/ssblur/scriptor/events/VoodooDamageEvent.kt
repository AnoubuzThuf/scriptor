package com.ssblur.scriptor.events


import com.ssblur.scriptor.ScriptorDamage
import com.ssblur.scriptor.ScriptorDamage.VOODOO
import com.ssblur.scriptor.ScriptorDamage.voodoo
import com.ssblur.scriptor.data.saved_data.VoodooSpellSavedData
import com.ssblur.scriptor.effect.VoodooStatusEffect
import com.ssblur.unfocused.event.common.EntityDamagedEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity

object VoodooDamageEvent {
    init {
        EntityDamagedEvent.Before.register { (entity, source, damage, event) ->
            if (source.type() != VOODOO) {
                val level = entity.level()
                if (entity.health > 0 && level is ServerLevel) {
                    val voodooEffects = entity.activeEffects.filter { it.effect.value() is VoodooStatusEffect && (it.duration > 0 || it.isInfiniteDuration) }
                    if (voodooEffects.size > 0) {
                        val voodooEffect = voodooEffects.first()
                        val redirectDamageMultiplier = (((voodooEffect.amplifier + 1) * 20).toFloat() / 100f).coerceIn(0.1f, 0.9f)
                        val voodooVictimIdHolder = VoodooSpellSavedData.computeIfAbsent(entity)
                        if (voodooVictimIdHolder != null && voodooVictimIdHolder.getSubjectId() != null) {
                            val victimEntity = level.getEntity(voodooVictimIdHolder.getSubjectId()!!)
                            if (victimEntity is LivingEntity && victimEntity.health > 0) {
                                if (victimEntity.position() != null && entity.position() != null && victimEntity.position().distanceTo(entity.position()) < 100) {
                                    if (damage <= 2) {

                                        victimEntity.hurt(voodoo(victimEntity), damage)
                                        event.cancel(0f)
                                    } else {
                                        victimEntity.hurt(voodoo(victimEntity), damage * redirectDamageMultiplier)
                                        event.cancel(damage * (1f - redirectDamageMultiplier))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}