package com.ssblur.scriptor.entity.goals

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.navigation.PathNavigation
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator
import java.util.*
import java.util.function.Supplier
import kotlin.math.abs


class GenericFollowOwnerGoal(
    private val mob: PathfinderMob,
    private val ownerGetter: Supplier<LivingEntity?>,
    private val speedModifier: Double,
    private val startDistance: Float,
    private val stopDistance: Float,
    canFly: Boolean,
    private val teleportDistance: Float
) : Goal() {
    private var owner: LivingEntity? = null
    private val navigation: PathNavigation
    private var timeToRecalcPath = 0
    private var oldWaterCost = 0f
    private val canFly: Boolean

    init {
        this.navigation = mob.getNavigation()
        this.setFlags(EnumSet.of<Flag?>(Flag.MOVE, Flag.LOOK))
        this.canFly = canFly
    }

    override fun canUse(): Boolean {
        val livingentity = this.ownerGetter.get()
        if (livingentity == null) {
            return false
        } else if (this.mob.distanceToSqr(livingentity) < (this.startDistance * this.startDistance).toDouble()) {
            return false
        } else {
            this.owner = livingentity
            return true
        }
    }

    override fun canContinueToUse(): Boolean {
        if (this.navigation.isDone()) {
            return false
        } else {
            return !(this.mob.distanceToSqr(this.owner) <= (this.stopDistance * this.stopDistance).toDouble())
        }
    }

    override fun start() {
        this.timeToRecalcPath = 0
        this.oldWaterCost = this.mob.getPathfindingMalus(PathType.WATER)
        this.mob.setPathfindingMalus(PathType.WATER, 0.0f)
    }

    override fun stop() {
        this.owner = null
        this.navigation.stop()
        this.mob.setPathfindingMalus(PathType.WATER, this.oldWaterCost)
    }

    override fun tick() {
        val flag = this.shouldTryTeleportToOwner()
        if (!flag) {
            this.mob.getLookControl().setLookAt(this.owner, 10.0f, this.mob.getMaxHeadXRot().toFloat())
        }

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10)
            if (flag) {
                this.tryToTeleportToOwner()
            } else {
                if (false && canFly && !mob.onGround()) {
                    val vec3 = owner!!.position()
                    this.mob.getMoveControl().setWantedPosition(vec3.x, vec3.y + 2, vec3.z, this.speedModifier)
                } else {
                    this.navigation.moveTo(this.owner, this.speedModifier)
                }
            }
        }
    }

    fun tryToTeleportToOwner() {
        val livingentity = this.ownerGetter.get()
        if (livingentity != null) {
            this.teleportToAroundBlockPos(livingentity.blockPosition())
        }
    }

    fun shouldTryTeleportToOwner(): Boolean {
        val livingentity = this.ownerGetter.get()
        return livingentity != null && mob.distanceToSqr(livingentity) >= teleportDistance * teleportDistance
    }

    private fun teleportToAroundBlockPos(pPos: BlockPos) {
        for (i in 0..9) {
            val j = mob.getRandom().nextIntBetweenInclusive(-3, 3)
            val k = mob.getRandom().nextIntBetweenInclusive(-3, 3)
            if (abs(j) >= 2 || abs(k) >= 2) {
                val l = mob.getRandom().nextIntBetweenInclusive(-1, 1)
                if (this.maybeTeleportTo(pPos.getX() + j, pPos.getY() + l, pPos.getZ() + k)) {
                    return
                }
            }
        }
    }

    private fun maybeTeleportTo(pX: Int, pY: Int, pZ: Int): Boolean {
        if (!this.canTeleportTo(BlockPos(pX, pY, pZ))) {
            return false
        } else {
            mob.moveTo(pX.toDouble() + 0.5, pY.toDouble(), pZ.toDouble() + 0.5, mob.getYRot(), mob.getXRot())
            this.navigation.stop()
            return true
        }
    }

    private fun canTeleportTo(pPos: BlockPos): Boolean {
        val pathtype = WalkNodeEvaluator.getPathTypeStatic(mob, pPos)
        if (pathtype != PathType.WALKABLE) {
            return false
        } else {
            val blockstate = mob.level().getBlockState(pPos.below())
            if (!this.canFly && blockstate.getBlock() is LeavesBlock) {
                return false
            } else {
                val blockpos = pPos.subtract(mob.blockPosition())
                return mob.level().noCollision(mob, mob.getBoundingBox().move(blockpos))
            }
        }
    }
}