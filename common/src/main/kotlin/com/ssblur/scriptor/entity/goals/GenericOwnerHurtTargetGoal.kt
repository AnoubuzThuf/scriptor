package com.ssblur.scriptor.entity.goals

import com.ssblur.scriptor.entity.IMagicSummon
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.goal.target.TargetGoal
import net.minecraft.world.entity.ai.targeting.TargetingConditions
import java.util.*
import java.util.function.Supplier


class GenericOwnerHurtTargetGoal(private val entity: Mob, private val owner: Supplier<LivingEntity?>) : TargetGoal(
    entity, false
) {
    private var ownerLastHurt: LivingEntity? = null
    private var timestamp = 0

    init {
        this.setFlags(EnumSet.of<Flag?>(Flag.TARGET))
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    override fun canUse(): Boolean {
        val owner = this.owner.get()
        if (owner == null) {
            return false
        } else {
            //mob.getLastHurtByMobTimestamp() == mob.tickCount - 1
            this.ownerLastHurt = owner.getLastHurtMob()
            val lastHurt = this.ownerLastHurt
            val i = owner.getLastHurtMobTimestamp()


            return i != this.timestamp && this.canAttack(
                lastHurt,
                TargetingConditions.DEFAULT
            ) && !(lastHurt is IMagicSummon && lastHurt.getSummoner() === owner)
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    override fun start() {
        this.mob.setTarget(this.ownerLastHurt)
        val owner = this.owner.get()
        if (owner != null) {
            this.timestamp = owner.getLastHurtMobTimestamp()
        }

        super.start()
    }
}