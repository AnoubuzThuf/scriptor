package com.ssblur.scriptor.entity

import com.ssblur.scriptor.entity.goals.*
import com.ssblur.scriptor.entity.utils.deserializeOwner
import com.ssblur.scriptor.entity.utils.getAndCacheOwner
import com.ssblur.scriptor.entity.utils.serializeOwner
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.RandomSource
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.monster.Vex
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import java.util.*


class SummonedVex(entityType: EntityType<SummonedVex?>?, level: Level): IMagicSummon,
    Vex(entityType, level) {

    fun setup(owner: LivingEntity, tag: CompoundTag, ticks: Int, hasLimitedLife: Boolean = true, power: Int = 1) {
        setSummoner(owner)
        xpReward = 0
        this.load(tag)
        this.setLimitedLifeTicks(ticks, hasLimitedLife)
        this.power = power
//                float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    @get:JvmName("jvm_summoner")
    @set:JvmName("jvm_summoner")
    var summoner: LivingEntity? = null
    var summonerUUID: UUID? = null
    var limitedLifeTicks: Int = 0
    var hasLimitedLife: Boolean = true
    var power: Int = 0

    override fun tick() {
        this.noPhysics = true
        super.tick()
        this.noPhysics = false
        this.setNoGravity(true)
        if (this.hasLimitedLife && --this.limitedLifeTicks <= 0) {
            this.limitedLifeTicks = 20
            this.hurt(this.damageSources().starve(), 10.0f)
        }
    }

    fun setSummoner(summoner: LivingEntity) {
        this.summoner = summoner
        this.summonerUUID = summoner.uuid
    }

    override fun getSummoner(): LivingEntity? {
        return getAndCacheOwner(level(), summoner, summonerUUID)
    }

    fun setLimitedLifeTicks(ticks: Int, hasLimitedLife: Boolean = true) {
        this.limitedLifeTicks = ticks
        this.hasLimitedLife = hasLimitedLife
    }

    override fun populateDefaultEquipmentSlots(randomSource: RandomSource, difficultyInstance: DifficultyInstance) {
        val weapon = when (this.power) {
            in 0..1 -> Items.STICK
            2 -> Items.WOODEN_SWORD
            3 -> Items.STONE_SWORD
            in 4..7 -> Items.IRON_SWORD
            in 8..12 -> Items.DIAMOND_SWORD
            else -> Items.NETHERITE_SWORD
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack(weapon))
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0f)
    }

    override fun registerGoals() {
        this.goalSelector.addGoal(0, FloatGoal(this))
        this.goalSelector.addGoal(4, this.VexChargeAttackGoal())
        this.goalSelector.addGoal(7, GenericFollowOwnerGoal(this, this::getSummoner, 0.65, 35f, 10f, true, 50f))
        this.goalSelector.addGoal(9, LookAtPlayerGoal(this, Player::class.java, 3.0f, 1.0f))
        this.goalSelector.addGoal(10, LookAtPlayerGoal(this, Mob::class.java, 8.0f))
        this.goalSelector.addGoal(16, this.VexRandomMoveGoal())

        this.targetSelector.addGoal(1, GenericOwnerHurtByTargetGoal(this, this::getSummoner))
        this.targetSelector.addGoal(2, GenericOwnerHurtTargetGoal(this, this::getSummoner))
        this.targetSelector.addGoal(3, GenericCopyOwnerTargetGoal(this, this::getSummoner))
        this.targetSelector.addGoal(4, GenericHurtByTargetGoal(this, { entity: Entity? -> entity == getSummoner() }).setAlertOthers())
        this.targetSelector.addGoal(5, GenericProtectOwnerTargetGoal(this, this::getSummoner))

    }

    override fun isPreventingPlayerRest(pPlayer: Player?): Boolean {
        return !this.isAlliedTo(pPlayer!!)
    }

    fun finalSpeed(): Double {
        return 1.0 * this.power
    }

    override fun die(damageSource: DamageSource) {
        this.onDeathHelper()
        super.die(damageSource)
    }

    override fun readAdditionalSaveData(compoundTag: CompoundTag) {
        super.readAdditionalSaveData(compoundTag)
        this.summonerUUID = deserializeOwner(compoundTag)
    }

    override fun addAdditionalSaveData(compoundTag: CompoundTag) {
        super.addAdditionalSaveData(compoundTag)
        serializeOwner(compoundTag, this.summonerUUID)
    }

    override fun isAlliedTo(entity: Entity): Boolean {
        return super.isAlliedTo(entity) || this.isAlliedHelper(entity)
    }

    override fun shouldDespawnInPeaceful(): Boolean {
        return false
    }

    inner class VexChargeAttackGoal: Goal() {
        fun VexChargeAttackGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE))
        }

        override fun canUse(): Boolean {
            val target: LivingEntity? = this@SummonedVex.getTarget()
            if (target != null && target.isAlive() && !this@SummonedVex.getMoveControl().hasWanted()
                && this@SummonedVex.random.nextInt(reducedTickDelay(7)) == 0) {
                return this@SummonedVex.distanceToSqr(target) > 4.0
            } else {
                return false
            }
        }

        override fun canContinueToUse(): Boolean {
            return this@SummonedVex.getMoveControl()
                .hasWanted() && this@SummonedVex.isCharging() && this@SummonedVex.getTarget() != null && this@SummonedVex.getTarget()!!
                .isAlive()
        }

        override fun start() {
            val livingentity = this@SummonedVex.getTarget()
            if (livingentity != null) {
                val vec3 = livingentity.getEyePosition()
                this@SummonedVex.moveControl.setWantedPosition(vec3.x, vec3.y, vec3.z, finalSpeed())
            }

            this@SummonedVex.setIsCharging(true)
            this@SummonedVex.playSound(SoundEvents.VEX_CHARGE, 1.0f, 1.0f)
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        override fun stop() {
            this@SummonedVex.setIsCharging(false)
        }

        override fun requiresUpdateEveryTick(): Boolean {
            return true
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        override fun tick() {
            val livingentity = this@SummonedVex.getTarget()
            if (livingentity != null) {
                if (this@SummonedVex.getBoundingBox().intersects(livingentity.getBoundingBox())) {
                    this@SummonedVex.doHurtTarget(livingentity)
                    this@SummonedVex.setIsCharging(false)
                } else {
                    val d0 = this@SummonedVex.distanceToSqr(livingentity)
                    if (d0 < 9.0) {
                        val vec3 = livingentity.getEyePosition()
                        this@SummonedVex.moveControl.setWantedPosition(vec3.x, vec3.y, vec3.z, finalSpeed())
                    }
                }
            }
        }
    }

    inner class VexRandomMoveGoal : Goal() {
        /**
         * Copy of private random move goal in vex
         */
        init {
            this.setFlags(EnumSet.of<Flag?>(Flag.MOVE))
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        override fun canUse(): Boolean {
            return !this@SummonedVex.getMoveControl()
                .hasWanted() && this@SummonedVex.random.nextInt(reducedTickDelay(7)) == 0
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        override fun canContinueToUse(): Boolean {
            return false
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        override fun tick() {
            var blockpos: BlockPos? = this@SummonedVex.getBoundOrigin()
            if (blockpos == null) {
                blockpos = this@SummonedVex.blockPosition()
            }

            for (i in 0..2) {
                val blockpos1 = blockpos!!.offset(
                    this@SummonedVex.random.nextInt(15) - 7,
                    this@SummonedVex.random.nextInt(11) - 5,
                    this@SummonedVex.random.nextInt(15) - 7
                )
                if (this@SummonedVex.level().isEmptyBlock(blockpos1)) {
                    this@SummonedVex.moveControl.setWantedPosition(
                        blockpos1.getX().toDouble() + 0.5,
                        blockpos1.getY().toDouble() + 0.5,
                        blockpos1.getZ().toDouble() + 0.5,
                        0.25 * finalSpeed()
                    )
                    if (this@SummonedVex.getTarget() == null) {
                        this@SummonedVex.getLookControl().setLookAt(
                            blockpos1.getX().toDouble() + 0.5,
                            blockpos1.getY().toDouble() + 0.5,
                            blockpos1.getZ().toDouble() + 0.5,
                            180.0f,
                            20.0f
                        )
                    }
                    break
                }
            }
        }
    }
}
