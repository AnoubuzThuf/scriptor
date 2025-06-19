package com.ssblur.scriptor.entity.goals

import com.ssblur.scriptor.entity.IMagicSummon
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.goal.target.TargetGoal
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import java.util.function.Supplier


class GenericCopyOwnerTargetGoal(pMob: PathfinderMob?, private val ownerGetter: Supplier<LivingEntity?>) :
    TargetGoal(pMob, false) {
    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    override fun canUse(): Boolean {
        val owner = ownerGetter.get()
        if (owner is Mob && owner.getTarget() != null) {
            val target = owner.getTarget()
            return owner is Mob && owner.getTarget() != null && !(target is IMagicSummon && target.getSummonerAlt() === owner)
        }
        return false
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    override fun start() {
        val target = (ownerGetter.get() as Mob).getTarget()
        mob.setTarget(target)

        if (this.ownerGetter.get()!!.uuid == target!!.uuid) {
            this.ownerGetter.get()!!.sendSystemMessage(Component.literal(this::class.java.toString()))
        }
        this.mob.getBrain().setMemoryWithExpiry<LivingEntity?>(MemoryModuleType.ATTACK_TARGET, target, 200L)

        super.start()
    }
}