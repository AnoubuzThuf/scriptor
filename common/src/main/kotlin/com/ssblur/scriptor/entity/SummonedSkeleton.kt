package com.ssblur.scriptor.entity

import com.ssblur.scriptor.entity.goals.*
import com.ssblur.scriptor.entity.utils.deserializeOwner
import com.ssblur.scriptor.entity.utils.getAndCacheOwner
import com.ssblur.scriptor.entity.utils.serializeOwner
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.RandomSource
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.attributes.AttributeInstance
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.*
import net.minecraft.world.entity.animal.Wolf
import net.minecraft.world.entity.monster.AbstractSkeleton
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

    fun setup(owner: LivingEntity, tag: CompoundTag, ticks: Int, hasLimitedLife: Boolean = true, power: Int = 1, color: Int = -6265536, isRanged: Boolean=false) {
        setSummoner(owner)
        xpReward = 0
        this.load(tag)
        this.setLimitedLifeTicks(ticks, hasLimitedLife)
        this.power = power
        this.color = color
        this.isRanged = isRanged
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

    override fun tick() {
        super.tick()
        if (this.hasLimitedLife && --this.limitedLifeTicks <= 0) {
            this.limitedLifeTicks = 20
            this.hurt(this.damageSources().starve(), 20.0f)
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
        this.goalSelector.addGoal(0, FloatGoal(this))
        this.goalSelector.addGoal(2, RestrictSunGoal(this))
        this.goalSelector.addGoal(3, FleeSunGoal(this, 1.0))
        this.goalSelector.addGoal(3, AvoidEntityGoal(this, Wolf::class.java, 6.0F, 1.0, 1.2))
        this.goalSelector.addGoal(4, GenericFollowOwnerGoal(this, this::getSummoner, 1.0, 15f, 5f, false, null))
        this.goalSelector.addGoal(5, WaterAvoidingRandomStrollGoal(this, 1.0))
        this.goalSelector.addGoal(6, LookAtPlayerGoal(this, Player::class.java, 8f))
        this.goalSelector.addGoal(6, RandomLookAroundGoal(this))
        this.targetSelector.addGoal(1, GenericOwnerHurtByTargetGoal(this, this::getSummoner))
        this.targetSelector.addGoal(2, GenericOwnerHurtTargetGoal(this, this::getSummoner))
        this.targetSelector.addGoal(3, GenericCopyOwnerTargetGoal(this, this::getSummoner))
        this.targetSelector.addGoal(4, GenericHurtByTargetGoal(this, { entity: Entity? -> entity == getSummoner() }).setAlertOthers())
        this.targetSelector.addGoal(5, GenericProtectOwnerTargetGoal(this, this::getSummoner))
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
                val bowPool = listOf(Items.BOW, Items.CROSSBOW)
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

