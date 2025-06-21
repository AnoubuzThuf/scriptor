package com.ssblur.scriptor.entity.goals

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.navigation.PathNavigation
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.function.Supplier
import kotlin.math.abs


class GenericSentryGoal(
    private val mob: PathfinderMob,
    private val speedModifier: Double,
    private val allowTeleport: Boolean = true
) : Goal() {
    private val navigation: PathNavigation
    private var timeBeforeTeleport = 0

    init {
        this.navigation = mob.getNavigation()
        this.setFlags(EnumSet.of<Flag?>(Flag.MOVE, Flag.LOOK))
    }

    override fun canUse(): Boolean {
        return (!this.mob.navigation.isDone())
    }

    override fun canContinueToUse(): Boolean {
        return !this.navigation.isDone()
    }

    override fun start() {
        this.timeBeforeTeleport = 1200
        this.mob.getMoveControl().setWantedPosition(this.mob.restrictCenter.x.toDouble(),
            this.mob.restrictCenter.y.toDouble(),
            this.mob.restrictCenter.z.toDouble(),
            this.speedModifier)
    }

    override fun stop() {
        this.navigation.stop()
    }

    override fun tick() {
        this.mob.getLookControl().setLookAt(
            Vec3(
                this.mob.restrictCenter.x.toDouble(),
                this.mob.restrictCenter.y.toDouble() + 1.0,
                this.mob.restrictCenter.z.toDouble()
            )
        )
        if (--this.timeBeforeTeleport <= 0) {
            if (allowTeleport) {
                this.tryToTeleportToPosition()
            }
            this.timeBeforeTeleport = 100
        }
    }

    fun tryToTeleportToPosition() {
        this.mob.moveTo(Vec3(
            this.mob.restrictCenter.x.toDouble(),
            this.mob.restrictCenter.y.toDouble(),
            this.mob.restrictCenter.z.toDouble()
        ))
        this.mob.navigation.stop()
    }
}