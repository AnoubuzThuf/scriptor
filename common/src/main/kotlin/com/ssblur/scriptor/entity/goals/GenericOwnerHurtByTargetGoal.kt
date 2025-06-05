package com.ssblur.scriptor.entity.goals

import com.ssblur.scriptor.entity.IMagicSummon
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.goal.target.TargetGoal
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.targeting.TargetingConditions
import java.util.*
import java.util.function.Supplier


class GenericOwnerHurtByTargetGoal(private val entity: Mob, private val owner: Supplier<LivingEntity?>) : TargetGoal(
    entity, false
) {
    private var ownerLastHurtBy: LivingEntity? = null
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
            this.ownerLastHurtBy = owner.getLastHurtByMob()
            val lastHurtBy = this.ownerLastHurtBy
            if (lastHurtBy == null || lastHurtBy!!.isAlliedTo(mob)) return false
            val i = owner.getLastHurtByMobTimestamp()

            return i != this.timestamp && this.canAttack(
                lastHurtBy,
                TargetingConditions.DEFAULT
            ) && !(lastHurtBy is IMagicSummon && lastHurtBy.getSummoner() === owner)
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    override fun start() {
        this.mob.setTarget(this.ownerLastHurtBy)
        this.mob.getBrain()
            .setMemoryWithExpiry<LivingEntity?>(MemoryModuleType.ATTACK_TARGET, this.ownerLastHurtBy, 200L)
        val owner = this.owner.get()
        if (owner != null) {
            this.timestamp = owner.getLastHurtByMobTimestamp()
        }

        super.start()
    }
}