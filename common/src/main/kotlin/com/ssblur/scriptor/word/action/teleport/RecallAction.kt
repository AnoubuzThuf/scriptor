package com.ssblur.scriptor.word.action.teleport

import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.api.word.Word
import com.ssblur.scriptor.data.components.ScriptorDataComponents
import com.ssblur.scriptor.data.saved_data.TeleportWaypointSavedData
import com.ssblur.scriptor.data.saved_data.TeleportWaypointSavedData.Companion.computeIfAbsent
import com.ssblur.scriptor.helpers.targetable.EntityTargetable
import com.ssblur.scriptor.helpers.targetable.LecternTargetable
import com.ssblur.scriptor.helpers.targetable.SpellbookTargetable
import com.ssblur.scriptor.helpers.targetable.Targetable
import com.ssblur.scriptor.helpers.targetable.WandTargetable
import com.ssblur.scriptor.network.client.ParticleNetwork
import com.ssblur.scriptor.word.descriptor.target.NetherDescriptor
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

fun stringToVec3(string: String?): Vec3? {
    if (string == null) return null
    val components: List<String> = string.split(',')
    if (components.size != 3) {
        return null
    }
    try {
        return Vec3(components[0].toDouble(), components[1].toDouble(), components[2].toDouble())
    } catch (e: NumberFormatException) {
    }
    return null
}

fun vec3ToString(pos: Vec3): String {
    return pos.x.toString() + ',' + pos.y.toString() + ',' + pos.z.toString()
}

class RecallAction: SwapAction() {
    override fun apply(caster: Targetable, targetable: Targetable, descriptors: Array<Descriptor>, words: Array<Word?>) {
        if (targetable.level.isClientSide) return
        if (caster.level.isClientSide) return
        val server = caster.level as ServerLevel
        val level = targetable.level

        val waypoint: Vec3? = if (caster is SpellbookTargetable) {
            val spellbook = caster.targetItem
            var spellbookWaypoint = stringToVec3(spellbook.get(ScriptorDataComponents.TELEPORT_WAYPOINT))

            if ((spellbookWaypoint == null) and (caster.targetEntity is Player)) {
                val casterEntity = caster.targetEntity as Player
                val casterWaypoint = computeIfAbsent(casterEntity)
                if ((casterWaypoint != null) and (casterWaypoint!!.coordinate != null)) {
                    spellbook.set(
                        ScriptorDataComponents.TELEPORT_WAYPOINT, vec3ToString(casterWaypoint.coordinate!!)
                    )
                    spellbookWaypoint = casterWaypoint.coordinate
                }
            }
            spellbookWaypoint
        } else if (caster is LecternTargetable) {
            val spellbook = caster.spellbook
            if (spellbook != null) {
                stringToVec3(spellbook.get(ScriptorDataComponents.TELEPORT_WAYPOINT))
            } else {
                null
            }
        } else if (caster is WandTargetable || caster is EntityTargetable) {
            val casterEntity = caster.targetEntity
            if (casterEntity is Player) {
                computeIfAbsent(casterEntity)!!.coordinate
            } else {
                null
            }
        } else {
            null
        }
        if (waypoint == null) {
            ParticleNetwork.fizzle(caster.level, caster.targetBlockPos)
            server.playSound(
                null,
                caster.targetBlockPos,
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS,
                1.0f,
                server.getRandom().nextFloat() * 0.4f + 0.8f
            )
            return
        }
        val markedLocation = Targetable(level, waypoint)
        teleport(targetable, markedLocation)
    }

    override fun cost() = Cost(6.0, COSTTYPE.ADDITIVE)
}
