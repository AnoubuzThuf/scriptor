package com.ssblur.scriptor.entity.goals

import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.goal.target.TargetGoal
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.targeting.TargetingConditions
import net.minecraft.world.level.GameRules
import net.minecraft.world.phys.AABB
import java.util.*
import java.util.function.Predicate


class GenericHurtByTargetGoal(pMob: PathfinderMob?, var toIgnoreDamage: Predicate<LivingEntity?>) :
    TargetGoal(pMob, true) {
    private var alertSameType = false

    /**
     * Store the previous revengeTimer value
     */
    private var timestamp = 0
    private var toIgnoreAlert: Array<out Class<*>?>? = null

    init {
        this.setFlags(EnumSet.of<Flag?>(Flag.TARGET))
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    override fun canUse(): Boolean {
        val i = this.mob.getLastHurtByMobTimestamp()
        val livingentity = this.mob.getLastHurtByMob()
        if (livingentity == null || livingentity.isAlliedTo(mob)) return false
        if (i != this.timestamp && livingentity != null) {
            if (livingentity.getType() === EntityType.PLAYER && this.mob.level().getGameRules()
                    .getBoolean(GameRules.RULE_UNIVERSAL_ANGER)
            ) {
                return false
            } else {
                if (toIgnoreDamage.test(livingentity)) return false

                return this.canAttack(livingentity, HURT_BY_TARGETING)
            }
        } else {
            return false
        }
    }

    fun setAlertOthers(vararg pReinforcementTypes: Class<*>?): GenericHurtByTargetGoal {
        this.alertSameType = true
        this.toIgnoreAlert = pReinforcementTypes
        return this
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    override fun start() {
        this.mob.setTarget(this.mob.getLastHurtByMob())
        this.mob.getBrain()
            .setMemoryWithExpiry<LivingEntity?>(MemoryModuleType.ATTACK_TARGET, this.mob.getLastHurtByMob(), 200L)

        this.targetMob = this.mob.getTarget()
        this.timestamp = this.mob.getLastHurtByMobTimestamp()
        this.unseenMemoryTicks = 300
        if (this.alertSameType) {
            this.alertOthers()
        }

        super.start()
    }

    protected fun alertOthers() {
        val d0 = this.getFollowDistance()
        val aabb = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(d0, 10.0, d0)
        val list = this.mob.level().getEntitiesOfClass(this.mob.javaClass, aabb, EntitySelector.NO_SPECTATORS)
        val iterator: MutableIterator<*> = list.iterator()

        while (true) {
            var mob: Mob
            while (true) {
                if (!iterator.hasNext()) {
                    return
                }

                mob = iterator.next() as Mob
                if (this.mob !== mob && mob.getTarget() == null && (this.mob !is TamableAnimal || (this.mob as TamableAnimal).getOwner() === (mob as TamableAnimal).getOwner()) && !mob.isAlliedTo(
                        this.mob.getLastHurtByMob()
                    )
                ) {
                    if (this.toIgnoreAlert == null) {
                        break
                    }

                    var flag = false

                    for (oclass in this.toIgnoreAlert) {
                        if (mob.javaClass == oclass) {
                            flag = true
                            break
                        }
                    }

                    if (!flag) {
                        break
                    }
                }
            }

            this.alertOther(mob, this.mob.getLastHurtByMob())
        }
    }

    protected fun alertOther(pMob: Mob, pTarget: LivingEntity?) {
        pMob.setTarget(pTarget)
    }

    companion object {
        private val HURT_BY_TARGETING: TargetingConditions =
            TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting()
        private const val ALERT_RANGE_Y = 10
    }
}