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
    private var timeToRecalcPath = 0
    private var timesRecalcluated = 0
    private val permittedTimesRecalculated = 100

    init {
        this.navigation = mob.getNavigation()
        this.setFlags(EnumSet.of<Flag?>(Flag.MOVE, Flag.LOOK))
    }

    override fun canUse(): Boolean {
        return (this.mob.isWithinRestriction())
    }

    override fun canContinueToUse(): Boolean {
        return (this.navigation.isDone() || this.mob.isWithinRestriction())
    }

    override fun start() {
        this.timesRecalcluated = 0
        this.timeToRecalcPath = 0
    }

    override fun stop() {
        this.navigation.stop()
    }

    override fun tick() {
        this.mob.getLookControl().setLookAt(
            Vec3(
                this.mob.restrictCenter.x.toDouble(),
                this.mob.restrictCenter.y.toDouble(),
                this.mob.restrictCenter.z.toDouble()
            )
        )

        if (--this.timeToRecalcPath <= 0) {
            this.timesRecalcluated += 1
            if (this.timesRecalcluated > this.permittedTimesRecalculated) {
                if (allowTeleport) {
                    this.tryToTeleportToPosition()
                }
            } else {
                this.timeToRecalcPath = this.adjustedTickDelay(10)
                this.mob.getMoveControl().setWantedPosition(this.mob.restrictCenter.x.toDouble(),
                    this.mob.restrictCenter.y.toDouble(),
                    this.mob.restrictCenter.z.toDouble(),
                    this.speedModifier)
            }
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