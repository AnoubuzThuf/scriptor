package com.ssblur.scriptor.word.action.teleport

import com.ssblur.scriptor.api.word.Action
import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.api.word.Word
import com.ssblur.scriptor.data.saved_data.TeleportWaypointSavedData
import com.ssblur.scriptor.data.saved_data.TeleportWaypointSavedData.Companion.computeIfAbsent
import com.ssblur.scriptor.helpers.targetable.EntityTargetable
import com.ssblur.scriptor.helpers.targetable.Targetable
import net.minecraft.world.entity.player.Player

class MarkAction: Action() {
    override fun apply(caster: Targetable, targetable: Targetable, descriptors: Array<Descriptor>, words: Array<Word?>) {
        if (caster is EntityTargetable) {
            val casterEntity = caster.targetEntity
            if (casterEntity is Player) {
                var waypoint: TeleportWaypointSavedData? = computeIfAbsent(casterEntity)
                if (waypoint != null) {
                    waypoint.coordinate = targetable.targetPos
                    waypoint.setDirty()
                }
            }
        }
    }

    override fun cost() = Cost(1.0, COSTTYPE.ADDITIVE)
}