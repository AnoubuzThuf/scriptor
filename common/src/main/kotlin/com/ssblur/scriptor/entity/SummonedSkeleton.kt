package com.ssblur.scriptor.entity

import com.ssblur.scriptor.entity.goals.*
import com.ssblur.scriptor.entity.utils.deserializeOwner
import com.ssblur.scriptor.entity.utils.getAndCacheOwner
import com.ssblur.scriptor.entity.utils.serializeOwner
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
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
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.*
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.animal.Wolf
import net.minecraft.world.entity.monster.AbstractSkeleton
import net.minecraft.world.entity.monster.Creeper
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.monster.Pillager
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.item.CrossbowItem
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

    fun setup(owner: LivingEntity, tag: CompoundTag, ticks: Int, hasLimitedLife: Boolean = true, power: Int = 1, color: Int = -6265536, isRanged: Boolean=false, isInvisible: Boolean=false) {
        setSummoner(owner)
        xpReward = 0
        this.load(tag)
        this.setLimitedLifeTicks(ticks, hasLimitedLife)
        this.power = power
        this.color = color
        this.isRanged = isRanged
        if (isInvisible) {
            val holder: Holder<MobEffect> = MobEffects.INVISIBILITY
            this.addEffect(MobEffectInstance(holder, -1))
        }
    }

    @get:JvmName("jvm_summoner")
    @set:JvmName("jvm_summoner")
    var summoner: LivingEntity? = null
    var summonerUUID: UUID? = null
    var limitedLifeTicks: Int = 0
    var hasLimitedLife: Boolean = true
    var power: Int = 0
    var color: Int = -6265536
    var isRanged: Boolean = false

    /**
     * setSentryGoal/setFollowSummonerGoal
     * setMonsterHunterGoal
     * setBerserkGoal
     */

    var randomStrollGoal: WaterAvoidingRandomStrollGoal? = null
    var restrictSunGoal: RestrictSunGoal? = null
    var fleeSunGoal: FleeSunGoal? = null
    var floatGoal: FloatGoal? = null
    fun setNormalMovementGoals(enabled: Boolean = true) {
        if (this.restrictSunGoal == null) {
            this.floatGoal = FloatGoal(this)
        }
        if (this.restrictSunGoal == null) {
            this.restrictSunGoal = RestrictSunGoal(this)
        }
        if (this.fleeSunGoal == null) {
            this.fleeSunGoal = FleeSunGoal(this, 1.0)
        }
        if (this.randomStrollGoal == null) {
            this.randomStrollGoal = WaterAvoidingRandomStrollGoal(this, 1.0)
        }
        this.goalSelector.removeGoal(this.restrictSunGoal!!)
        this.goalSelector.removeGoal(this.fleeSunGoal!!)
        this.goalSelector.removeGoal(this.randomStrollGoal!!)
        if (enabled) {
            this.goalSelector.addGoal(0, this.floatGoal!!)
            this.goalSelector.addGoal(2, this.restrictSunGoal!!)
            this.goalSelector.addGoal(3, this.fleeSunGoal!!)
            this.goalSelector.addGoal(6,this.randomStrollGoal!!)
        }
    }

    var sentryGoal: MoveTowardsRestrictionGoal? = null
    fun setSentryGoal(enabled: Boolean, pos: BlockPos?, range: Int) {
        if (enabled) {
            this.setFollowSummonerGoal(false)
//            this.setNormalMovementGoals(false)
        }
        if (this.sentryGoal == null) {
            this.sentryGoal = MoveTowardsRestrictionGoal(this, 1.0)
        }
        this.goalSelector.removeGoal(this.sentryGoal!!)
        if (enabled) {
            if (pos == null) {
                this.restrictTo(this.blockPosition(), range)
            } else {
                this.restrictTo(pos, range)
            }
            this.goalSelector.addGoal(5, this.sentryGoal!!)
            this.sentryGoal!!.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        } else {
            this.clearRestriction()
            this.setNormalMovementGoals(true)
        }
    }

    var followSummonerGoal: GenericFollowOwnerGoal? = null
    fun setFollowSummonerGoal(enabled: Boolean) {
        if (enabled) {
            this.setSentryGoal(false, null, 0)
        }
        if (this.followSummonerGoal == null) {
            this.followSummonerGoal = GenericFollowOwnerGoal(this, this::getSummoner, 1.0, 15f, 5f, false, 50f)
        }
        this.goalSelector.removeGoal(this.followSummonerGoal!!)
        if (enabled) {
            this.goalSelector.addGoal(5, this.followSummonerGoal!!)
        }
    }

    var monsterHunterGoal: NearestAttackableTargetGoal<Monster>? = null
    fun setMonsterHunterGoal(enabled: Boolean) {
        if (this.monsterHunterGoal == null) {
            this.monsterHunterGoal = NearestAttackableTargetGoal(this, Monster::class.java, 10, true, false,
                {
                    entity: LivingEntity -> entity is Monster && entity !is IMagicSummon && entity !is Creeper
                }
            )
        }
        this.targetSelector.removeGoal(this.monsterHunterGoal!!)
        if (enabled) {
            this.targetSelector.addGoal(11, this.monsterHunterGoal!!)
        }
    }

    var berserkGoal: NearestAttackableTargetGoal<LivingEntity>? = null
    fun setBerserkGoal(enabled: Boolean) {
        if (this.berserkGoal == null) {
            this.berserkGoal = NearestAttackableTargetGoal(this, LivingEntity::class.java, true)
        }
        this.targetSelector.removeGoal(this.berserkGoal!!)
        if (enabled) {
            this.summoner = null
            this.targetSelector.removeAllGoals{true}
            this.targetSelector.addGoal(1, this.berserkGoal!!)
        }
    }

    override fun tick() {
        super.tick()
        if (!this.level().isClientSide) {
            if (this.hasLimitedLife && --this.limitedLifeTicks <= 0) {
                spawnPoof(this.level() as ServerLevel, this.blockPosition())
                this.remove(RemovalReason.DISCARDED)

//            this.limitedLifeTicks = 20
//            this.hurt(this.damageSources().starve(), 20.0f)
            }
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

    override fun registerGoals() {
        this.setNormalMovementGoals(true)
        this.goalSelector.addGoal(3, AvoidEntityGoal(this, Wolf::class.java, 6.0F, 1.0, 1.2))
        //        Sentry or Follow Player Goal
        this.goalSelector.addGoal(8, LookAtPlayerGoal(this, Player::class.java, 8f))
        this.goalSelector.addGoal(8, RandomLookAroundGoal(this))
//        Berserk goal
        this.targetSelector.addGoal(0, GenericOwnerHurtByTargetGoal(this, this::getSummoner))
        this.targetSelector.addGoal(5, GenericOwnerHurtTargetGoal(this, this::getSummoner))
        this.targetSelector.addGoal(6, GenericCopyOwnerTargetGoal(this, this::getSummoner))
        this.targetSelector.addGoal(8, GenericHurtByTargetGoal(this, { entity: Entity? -> entity == getSummoner() }).setAlertOthers())
        this.targetSelector.addGoal(10, GenericProtectOwnerTargetGoal(this, this::getSummoner))
//        monsterHunterGoal = 11
    }


    override fun isPreventingPlayerRest(pPlayer: Player?): Boolean {
        return !this.isAlliedTo(pPlayer!!)
    }

    override fun die(damageSource: DamageSource) {
        this.onDeathHelper()
        super.die(damageSource)
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

