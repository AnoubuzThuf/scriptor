package com.ssblur.scriptor.entity.goals

import com.ssblur.scriptor.entity.IMagicSummon
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
import net.minecraft.world.entity.ai.util.LandRandomPos
import net.minecraft.world.phys.Vec3
import java.util.*

class GenericBoundedWanderGoal(val pMob: PathfinderMob, val distance: Double): WaterAvoidingRandomStrollGoal(pMob, 0.5) {
    var currentOrigin: BlockPos? = null

    fun GenericBoundedWanderGoal() {
        this.setFlags(EnumSet.of<Goal.Flag?>(Goal.Flag.MOVE))
    }

    override fun canUse(): Boolean {
        if (this.mob.getRandom().nextInt(reducedTickDelay(this.interval)) != 0) return false

        if (pMob is IMagicSummon) {
            val summon = pMob as IMagicSummon
            val blockPos: BlockPos? = summon.summonBoundOrigin
            if (blockPos != null) {
                if (blockPos.distToCenterSqr(pMob.position()) > (distance)) {
                    this.wantedX = blockPos.center.x
                    this.wantedY = blockPos.center.y
                    this.wantedZ = blockPos.center.z
                    this.forceTrigger = false
                    currentOrigin = blockPos
                    return true
                }

                val vec3 = getPosition(blockPos)
                if (vec3 == null) {
                    return false
                } else {
                    this.wantedX = vec3.x
                    this.wantedY = vec3.y
                    this.wantedZ = vec3.z
                    this.forceTrigger = false
                    currentOrigin = blockPos
                    return true
                }
            }
        }
        return false
    }

    override fun canContinueToUse(): Boolean {
        if (pMob is IMagicSummon) {
            val summon = pMob as IMagicSummon
            if (summon.summonBoundOrigin != currentOrigin) {
                currentOrigin = null
                return false
            }
        }
        return super.canContinueToUse()
    }

    fun getPosition(blockPos: BlockPos): Vec3? {
        if (this.mob.isInWaterOrBubble()) {
            for (i in 1..5) {
                val vec3 = LandRandomPos.getPos(this.mob, 15, 7)
                if (vec3 != null && vec3.distanceToSqr(blockPos.center) < distance) {
                    return vec3
                }
            }
        } else {
            for (i in 1..5) {
                val vec3 = if (this.mob.getRandom().nextFloat() >= this.probability) LandRandomPos.getPos(this.mob, 10, 7)
                else super.getPosition()
                if (vec3 != null && vec3.distanceToSqr(blockPos.center) < distance) {
                    return vec3
                }
            }
        }
        return null
    }
}