package com.ssblur.scriptor.entity

import com.ssblur.scriptor.entity.goals.*
import com.ssblur.scriptor.word.descriptor.summon.SummonBehaviourDescriptor
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.RandomSource
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.ai.goal.target.TargetGoal
import net.minecraft.world.entity.monster.Creeper
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.monster.Vex
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import java.util.*


class SummonedVex(entityType: EntityType<SummonedVex?>?, level: Level): IMagicSummon,
    Vex(entityType, level) {

//    @get:JvmName("jvm_summoner")
//    @set:JvmName("jvm_summoner")
    override var summoner: LivingEntity? = null

    override var summonerUUID: UUID? = null

    var limitedLifeTicks: Int = 0
    var hasLimitedLife: Boolean = true
    var power: Int = 0
    var color: Int = -6265536
    //    Don't want to have ranged Vexes
    var isRanged: Boolean = false

    override var AI_ROUTINE_INDEX: Int? = null

    fun setSummonParams(summoner: LivingEntity?, limitedLifeTicks: Int = 100, hasLimitedLife: Boolean = true, power: Int, color: Int = -6265536, behaviourDescriptors: List<SummonBehaviourDescriptor>? = null, level: Level?) {
        if (level != null && !level.isClientSide) {
            setSummonerAlt(summoner)
//            if (this.getSummonerAlt() == null) {
//                this.level().players().first().sendSystemMessage(Component.literal("Summoner is null " + summoner!!.uuid.toString()))
//            }
            this.limitedLifeTicks = limitedLifeTicks
            this.hasLimitedLife = hasLimitedLife
            this.color = color
            this.power = power
            if (behaviourDescriptors == null) {
                setAiRoutineIndex(calculateAiRoutineIndex(null))
            } else {
                setAiRoutineIndex(calculateAiRoutineIndex(behaviourDescriptors.map{it.behaviour}))
            }
            this.goalSelector.removeAllGoals { true }
            this.targetSelector.removeAllGoals { true }

            this.registerGoals()
//            for (g in this.targetSelector.availableGoals) {
//                this.level().players().first().sendSystemMessage(Component.literal("target " + g.goal::class.java.toString()))
//            }
//            for (g in this.goalSelector.availableGoals) {
//                this.level().players().first().sendSystemMessage(Component.literal("goal " + g.goal::class.java.toString()))
//            }
            val tag = CompoundTag()
            this.addAdditionalSaveData(tag)
		}
    }

    override fun readAdditionalSaveData(compoundTag: CompoundTag) {
        super.readAdditionalSaveData(compoundTag)
        if (compoundTag.contains("AiRoutineIndex")) {
            this.AI_ROUTINE_INDEX = (compoundTag.getInt("AiRoutineIndex"))
        }
        if (compoundTag.contains("SummonerUUID")) {
            val uuid = compoundTag.getUUID("SummonerUUID")
            if (uuid != null) {
                this.summonerUUID = compoundTag.getUUID("SummonerUUID")
            }
        } else {
            null
        }
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)

        val aiRoutineIndex = this.AI_ROUTINE_INDEX
        if (aiRoutineIndex != null) {
            compound.putInt("AiRoutineIndex", aiRoutineIndex)
        }
        if (this.summoner != null) {
            compound.putUUID("SummonerUUID", this.summoner!!.uuid)
        } else {
            if (this.summonerUUID == null || this.summonerUUID == Util.NIL_UUID) {
                compound.putUUID("SummonerUUID", Util.NIL_UUID)
            } else {
                compound.putUUID("SummonerUUID", this.summonerUUID!!)
            }
        }
    }

    override fun tick() {this.noPhysics = true
        this.noPhysics = false
        this.setNoGravity(true)
        super.tick()

//        if (this.getSummonerAlt() != null) {
//            for (g in this.targetSelector.availableGoals) {
//                if (g.isRunning) {
//                    this.getSummonerAlt()!!.sendSystemMessage(Component.literal("goal " + g::class.java.toString()))
//                }
//            }
//        }

        this.noPhysics = false
        this.setNoGravity(true)
        if (!this.level().isClientSide) {
            if (this.hasLimitedLife && --this.limitedLifeTicks <= 0) {
                spawnPoof(this.level() as ServerLevel, this.blockPosition())
                this.remove(RemovalReason.DISCARDED)
            }
        }
    }

    override fun getBaseExperienceReward(): Int {
        return 0
    }

    override fun registerGoals() {
        val routine_index = this.AI_ROUTINE_INDEX
        if (routine_index == null) {
            return
        }
//        if (this.getSummonerAlt() != null) {
//            this.getSummonerAlt()!!.sendSystemMessage(Component.literal("INDEX " + routine_index.toString()))
//        }
//        GOALS
//        PRIORITY 0
        this.goalSelector.addGoal(0, FloatGoal(this))
//        PRIORITY 4
        this.goalSelector.addGoal(4, SummonedVexChargeAttackGoal())
//        PRIORITY 5
        if (routine_index in 8..11) {
            val moveTowardsRestrictionGoal = GenericSentryGoal(this, 0.6, true)
            this.goalSelector.addGoal(5, moveTowardsRestrictionGoal)
            moveTowardsRestrictionGoal.setFlags(EnumSet.of<Goal.Flag?>(Goal.Flag.MOVE, Goal.Flag.LOOK))
        }
        if (routine_index in 4..7) {
            this.goalSelector.addGoal(5, GenericFollowOwnerGoal(this, this::getSummonerAlt, 1.0, 15.0f, 10.0f, true, 50f))
        }
//        PRIORITY 8
        this.goalSelector.addGoal(8, LookAtPlayerGoal(this, Player::class.java, 3.0f, 1.0f))
        this.goalSelector.addGoal(8, LookAtPlayerGoal(this, Mob::class.java, 8.0f))

//        PRIORITY 9
        if (routine_index in 0 .. 7) {
            this.goalSelector.addGoal(9, this.SummonedVexRandomMoveGoal())
        }

//        TARGETS
//        Priority 0
        if (routine_index in BERSERK_INDEXES) {
            this.targetSelector.addGoal(0, NearestAttackableTargetGoal(this, LivingEntity::class.java, 10, true, false,
                {
                        entity: LivingEntity -> true
                }
            ))
            return
        } else {
            this.targetSelector.addGoal(0, GenericOwnerHurtByTargetGoal(this, this::getSummonerAlt))
            this.targetSelector.addGoal(1, GenericOwnerHurtTargetGoal(this, this::getSummonerAlt))
            this.targetSelector.addGoal(2, GenericCopyOwnerTargetGoal(this, this::getSummonerAlt))
            this.targetSelector.addGoal(3, GenericHurtByTargetGoal(this, { entity: Entity? -> if (getSummonerAlt() != null && entity != null) entity.uuid == getSummonerAlt()!!.uuid else false }).setAlertOthers())
//            this.targetSelector.addGoal(10, GenericProtectOwnerTargetGoal(this, this::getSummoner))
        }


//        Priority 5
        if (routine_index in MONSTER_HUNT_INDEXES) {
            this.targetSelector.addGoal(5, NearestAttackableTargetGoal(this, Monster::class.java, 10, true, false,
                {
                        entity: LivingEntity -> entity is Monster && entity !is IMagicSummon && entity !is Creeper
                }
            ))
        }
        if (routine_index in OTHER_PLAYER_HUNT_INDEXES) {
//            Hunt non-allied summons before players
            this.targetSelector.addGoal(5, NearestAttackableTargetGoal(this, Monster::class.java, 10, true, false,
                {
                        entity: LivingEntity -> entity is IMagicSummon && !isAlliedHelper(entity) && entity !is Creeper
                }
            ))
        }
//        Priority 6
        if (routine_index in OTHER_PLAYER_HUNT_INDEXES) {
            this.targetSelector.addGoal(6, NearestAttackableTargetGoal(this, Monster::class.java, 10, true, false,
                {
                        entity: LivingEntity ->
                    if (this.getSummonerAlt() == null) {
                        entity is Player
                    } else {
                        entity is Player && entity != this.getSummonerAlt()
                    }
                }
            ))
        }
    }

    override fun shouldDespawnInPeaceful(): Boolean {
        return false

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

    inner class SummonedVexChargeAttackGoal: Goal() {
        init {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
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
                this@SummonedVex.moveControl.setWantedPosition(vec3.x, vec3.y, vec3.z, 1.0)
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
                        this@SummonedVex.moveControl.setWantedPosition(vec3.x, vec3.y, vec3.z, 1.0)
                    }
                }
            }
        }
    }

    inner class SummonedVexRandomMoveGoal : Goal() {
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
                        0.25
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
