package com.ssblur.scriptor.entity

import com.ssblur.scriptor.entity.goals.*
import com.ssblur.scriptor.word.descriptor.summon.SummonBehaviourDescriptor
import net.minecraft.Util
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.RandomSource
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.attributes.AttributeInstance
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.*
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.animal.Wolf
import net.minecraft.world.entity.monster.AbstractSkeleton
import net.minecraft.world.entity.monster.Creeper
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.item.component.DyedItemColor
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import java.util.*


class SummonedSkeleton(entityType: EntityType<SummonedSkeleton?>?, level: Level): IMagicSummon,
    AbstractSkeleton(entityType, level) {


//    @get:JvmName("jvm_summoner")
//    @set:JvmName("jvm_summoner")
    override var summoner: LivingEntity? = null

    override var summonerUUID: UUID? = null

    var limitedLifeTicks: Int? = 0
    var power: Int = 0
    var color: Int = -6265536
    //    Don't want to have ranged Vexes
    var isRanged: Boolean = false

    override var AI_ROUTINE_INDEX: Int? = null

    fun initSummon() {
        if (this.level() != null && !this.level().isClientSide) {
            this.goalSelector.removeAllGoals { true }
            this.targetSelector.removeAllGoals { true }
            this.registerGoals()
            this.reassessWeaponGoal()
        }
    }

    fun setSummonParams(summoner: LivingEntity?, limitedLifeTicks: Int? = null, power: Int = 0, color: Int = -6265536, behaviourDescriptors: List<SummonBehaviourDescriptor>? = null, level: Level?, isRanged: Boolean = false, isInvisible: Boolean = false) {
        if (level != null && !level.isClientSide) {
            setSummonerAlt(summoner)
            this.limitedLifeTicks = limitedLifeTicks
            this.power = power
            this.color = color

            if (summoner != null) {
                this.setCustomName(Component.literal(summoner.getCustomName()!!.getString() + "'s Summoned Skeleton"))
            }

            this.isRanged = isRanged
            if (isInvisible) {
                val holder: Holder<MobEffect> = MobEffects.INVISIBILITY
                this.addEffect(MobEffectInstance(holder, -1))
            }

            if (behaviourDescriptors == null) {
                setAiRoutineIndex(calculateAiRoutineIndex(null))
            } else {
                setAiRoutineIndex(calculateAiRoutineIndex(behaviourDescriptors.map{it.behaviour}))
            }

            val tag = CompoundTag()
            this.addAdditionalSaveData(tag)
            this.readAdditionalSaveData(tag)

//            if (this.getSummonerAlt() != null) {
//                for (t in this.goalSelector.availableGoals) {
//                    this.getSummonerAlt()!!.sendSystemMessage(Component.literal("Goals: " + t.goal::class.java.toString()))
//                }
//                for (t in this.targetSelector.availableGoals) {
//                    this.getSummonerAlt()!!.sendSystemMessage(Component.literal("Target Goals: " + t.goal::class.java.toString()))
//                }
//
//            }
        }
    }

    override fun readAdditionalSaveData(compoundTag: CompoundTag) {
        super.readAdditionalSaveData(compoundTag)
        if (compoundTag.contains("AiRoutineIndex")) {
            this.AI_ROUTINE_INDEX = (compoundTag.getInt("AiRoutineIndex"))
        }
        if (compoundTag.contains("lifetimeLimitedTicks")) {
            this.limitedLifeTicks = (compoundTag.getInt("lifetimeLimitedTicks"))
        }
        if (compoundTag.contains("SummonerUUID")) {
            val uuid = compoundTag.getUUID("SummonerUUID")
            if (uuid != null) {
                this.summonerUUID = compoundTag.getUUID("SummonerUUID")
            }
        } else {
            null
        }
        this.initSummon()
        this.reassessWeaponGoal()
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        val limitedTicks = this.limitedLifeTicks
        if (limitedTicks != null) {
            compound.putInt("lifetimeLimitedTicks", limitedTicks)
        }

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

    override fun tick() {
        super.tick()
        if (!this.level().isClientSide) {
//            if (this.getSummonerAlt() != null) {
//                this.target
//                for (t in this.goalSelector.availableGoals) {
//                    this.getSummonerAlt()!!.sendSystemMessage(Component.literal("Goals: " + t.goal::class.java.toString()))
//                }
//                for (t in this.targetSelector.availableGoals) {
//                    this.getSummonerAlt()!!.sendSystemMessage(Component.literal("Target Goals: " + t.goal::class.java.toString()))
//                }
//            }

            var remainingTicks = this.limitedLifeTicks
            if (remainingTicks != null && --remainingTicks <= 0) {
                spawnPoof(this.level() as ServerLevel, this.blockPosition())
                this.remove(RemovalReason.DISCARDED)
            }
            this.limitedLifeTicks = remainingTicks
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
//        GOALS
//        PRIORITY 0
        this.goalSelector.addGoal(0, FloatGoal(this))
//        PRIORITY 1
        this.goalSelector.addGoal(1, RestrictSunGoal(this))
//        PRIORITY 2
        this.goalSelector.addGoal(2, AvoidEntityGoal(this, Wolf::class.java, 6.0F, 1.0, 1.2))
        if (routine_index in 4..7) {
            this.goalSelector.addGoal(2, GenericFollowOwnerGoal(this, this::getSummonerAlt, 1.0, 10f, 2f, false, 50f))
        }
//        PRIORITY 3
        this.goalSelector.addGoal(3, FleeSunGoal(this, 1.0))
//        PRIORITY 4
//        Skeleton Ranged Attack Goal
//        PRIORITY 5
        if (routine_index in 8..11) {
            val moveTowardsRestrictionGoal = GenericSentryGoal(this, 1.0, true)
            this.goalSelector.addGoal(5, moveTowardsRestrictionGoal)
            moveTowardsRestrictionGoal.setFlags(EnumSet.of<Goal.Flag?>(Goal.Flag.MOVE, Goal.Flag.LOOK))
        }
        this.goalSelector.addGoal(8, LookAtPlayerGoal(this, Player::class.java, 3.0f, 1.0f))
        this.goalSelector.addGoal(8, RandomLookAroundGoal(this))
//        PRIORITY 6
        if (routine_index in 0 .. 7) {
            this.goalSelector.addGoal(9,WaterAvoidingRandomStrollGoal(this, 1.0))
        }

//        TARGETS
//        Priority 0
        if (routine_index in BERSERK_INDEXES) {
            this.targetSelector.addGoal(0, GenericHurtByTargetGoal(this, { entity: Entity? -> false }))
            this.targetSelector.addGoal(1, NearestAttackableTargetGoal(this, Player::class.java, 5, false, false, null))
            this.targetSelector.addGoal(2, NearestAttackableTargetGoal(this, LivingEntity::class.java, 5, false, false, null))
            return
        } else {
            this.targetSelector.addGoal(0, GenericOwnerHurtByTargetGoal(this, this::getSummonerAlt))
            this.targetSelector.addGoal(1, GenericOwnerHurtTargetGoal(this, this::getSummonerAlt))
            this.targetSelector.addGoal(2, GenericCopyOwnerTargetGoal(this, this::getSummonerAlt))
            this.targetSelector.addGoal(3, GenericHurtByTargetGoal(this, { entity: Entity? -> entity == getSummonerAlt() }).setAlertOthers())
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
            this.targetSelector.addGoal(6, NearestAttackableTargetGoal(this, Player::class.java, 10, true, false,
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

    override fun isPreventingPlayerRest(pPlayer: Player): Boolean {
        return !this.isAlliedHelper(pPlayer)
    }

    override fun getAmbientSound(): SoundEvent {
        return SoundEvents.SKELETON_AMBIENT
    }

    override fun getHurtSound(damageSource: DamageSource): SoundEvent {
        return SoundEvents.SKELETON_HURT
    }

    override fun getDeathSound(): SoundEvent {
        return SoundEvents.SKELETON_DEATH
    }

    fun getStepSound(): SoundEvent {
        return SoundEvents.SKELETON_STEP
    }

    override fun shouldDespawnInPeaceful(): Boolean {
        return false
    }

    override fun finalizeSpawn(
        serverLevelAccessor: ServerLevelAccessor,
        difficultyInstance: DifficultyInstance,
        mobSpawnType: MobSpawnType,
        spawnGroupData: SpawnGroupData?
    ): SpawnGroupData? {

        val randomSource = serverLevelAccessor.getRandom()
        val attributeInstance =
            Objects.requireNonNull<AttributeInstance?>(this.getAttribute(Attributes.FOLLOW_RANGE)) as AttributeInstance
        if (!attributeInstance.hasModifier(RANDOM_SPAWN_BONUS_ID)) {
            attributeInstance.addPermanentModifier(
                AttributeModifier(
                    RANDOM_SPAWN_BONUS_ID,
                    randomSource.triangle(0.0, 0.11485000000000001),
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                )
            )
        }

        this.setLeftHanded(randomSource.nextFloat() < 0.05f)

        val boots = ItemStack(Items.LEATHER_BOOTS)
        val helmet = ItemStack(Items.LEATHER_HELMET)
        val chestplate = ItemStack(Items.LEATHER_CHESTPLATE)
        val leggings = ItemStack(Items.LEATHER_LEGGINGS)
        boots.set(DataComponents.DYED_COLOR, DyedItemColor(this.color, false))
        helmet.set(DataComponents.DYED_COLOR, DyedItemColor(this.color, false))
        chestplate.set(DataComponents.DYED_COLOR, DyedItemColor(this.color, false))
        leggings.set(DataComponents.DYED_COLOR, DyedItemColor(this.color, false))
        var unequipped: ArrayList<Pair<ItemStack, EquipmentSlot>> = arrayListOf(
            Pair(boots, EquipmentSlot.FEET),
            Pair(helmet, EquipmentSlot.HEAD),
            Pair(chestplate, EquipmentSlot.CHEST),
            Pair(leggings, EquipmentSlot.LEGS))
        unequipped.shuffle()
        for (i in 0..unequipped.size-1) {
            if (this.power >= i + 1) {
                this.setItemSlot(unequipped.get(i).second, unequipped.get(i).first)
                this.setDropChance(unequipped.get(i).second, 0.0F);
            }
        }

        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F)
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F)
        this.setCanPickUpLoot(false)
//        Set weapon
        if (!this.isRanged) {
            if (this.power >= 1) {
                this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack(Items.SHIELD))
                val weaponPool = if (this.power < 4) {
                    listOf(Items.FISHING_ROD, Items.WOODEN_HOE, Items.WOODEN_SHOVEL, Items.WOODEN_PICKAXE)
                } else if (this.power in 4..6) {
                    listOf(Items.STONE_SWORD, Items.STONE_AXE)
                } else if (this.power in 7..29) {
                    listOf(Items.IRON_SWORD, Items.IRON_AXE)
                } else {
                    listOf(Items.NETHERITE_SWORD, Items.NETHERITE_AXE)
                }
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack(weaponPool.random()))
            }
        } else {
            if (this.power >= 1) {
                val bowPool = listOf(Items.BOW)
//                val bowPool = listOf(Items.BOW, Items.CROSSBOW)
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack(bowPool.random()))
            }
//            Scale power by giving tipped arrows
            if (this.power >= 4) {
                val potionPool: Holder<Potion> = listOf(Potions.POISON, Potions.SLOWNESS, Potions.WEAKNESS).random()
                val arrows: ItemStack = ItemStack(Items.TIPPED_ARROW)
                arrows.set(DataComponents.POTION_CONTENTS, PotionContents(potionPool))
                arrows.setCount(this.power * 2)
                this.setItemSlot(EquipmentSlot.OFFHAND, arrows)
            } else {
                this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack(Items.ARROW))
            }
        }
        this.populateDefaultEquipmentEnchantments(serverLevelAccessor, randomSource, difficultyInstance)
        this.reassessWeaponGoal()

        return spawnGroupData
    }
    override fun populateDefaultEquipmentEnchantments(
        serverLevelAccessor: ServerLevelAccessor,
        randomSource: RandomSource,
        difficultyInstance: DifficultyInstance
    ) {
        this.enchantSpawnedEquipment(serverLevelAccessor, EquipmentSlot.MAINHAND, randomSource, 0.25f, difficultyInstance)

        for (equipmentSlot in EquipmentSlot.entries) {
            if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                this.enchantSpawnedEquipment(serverLevelAccessor, equipmentSlot, randomSource, 0.5f, difficultyInstance)
            }
        }
    }

    private fun enchantSpawnedEquipment(
        serverLevelAccessor: ServerLevelAccessor,
        equipmentSlot: EquipmentSlot,
        randomSource: RandomSource,
        f: Float,
        difficultyInstance: DifficultyInstance
    ) {
        val itemStack = this.getItemBySlot(equipmentSlot)
        val powerModifier = if (this.power > 7) (this.power - 7) / 5f else 0f
        if (!itemStack.isEmpty() && randomSource.nextFloat() < f * powerModifier) {
            EnchantmentHelper.enchantItemFromProvider(
                itemStack,
                serverLevelAccessor.registryAccess(),
                VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT,
                difficultyInstance,
                randomSource
            )
            this.setItemSlot(equipmentSlot, itemStack)
        }
    }
}

