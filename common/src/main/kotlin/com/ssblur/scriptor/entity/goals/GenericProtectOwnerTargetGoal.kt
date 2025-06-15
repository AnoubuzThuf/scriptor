package com.ssblur.scriptor.entity.goals

import com.ssblur.scriptor.entity.IMagicSummon
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.goal.target.TargetGoal
import java.util.*
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.function.ToDoubleFunction
import kotlin.math.max


class GenericProtectOwnerTargetGoal(entity: Mob?, private val owner: Supplier<LivingEntity?>) :
    TargetGoal(entity, false) {
    private var intervalToCheck = 0
    private val maxIntensity = 100 // tick delay at minimum intensity

    /**
     * integer that slowly decays after repeated failed checks, meaning we can check less frequently
     */
    private var currentIntensity = 0

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
            if (--intervalToCheck <= 0) {
                val target = mob.getTarget()
                val entities: List<Mob?> = owner.level().getEntitiesOfClass<Mob?>(
                    Mob::class.java,
                    owner.getBoundingBox().inflate(16.0, 8.0, 16.0),
                    Predicate { mob: Mob? ->
                        target != null &&
                                (target!!
                                    .getUUID() == owner.getUUID() || (target is IMagicSummon && target.getSummonerAlt() != null && target.getSummonerAlt()!!
                                    .getUUID() == owner.getUUID()))
                    }
                )
                if (entities.isEmpty()) {
                    currentIntensity = max(0, currentIntensity - 10)
                    return false
                } else {
                    mob.setTarget(
                        entities.stream()
                            .min(Comparator.comparingDouble<Mob?>(ToDoubleFunction { o: Mob? -> o!!.distanceToSqr(owner) }))
                            .orElse(entities.get(0))
                    )
                    return true
                }
            } else {
                val i = owner.getLastHurtByMobTimestamp()
                val tick = owner.tickCount
                val combatIntervalModifier = Math.clamp(((tick - i) / 5).toLong(), 0, 200)
                val intensityModifier = maxIntensity - currentIntensity
                intervalToCheck = 20 + combatIntervalModifier + intensityModifier
            }
        }
        return false
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    override fun start() {
        currentIntensity = maxIntensity
        super.start()
    }
}