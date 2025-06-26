package com.ssblur.scriptor.entity

import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.OwnableEntity
import net.minecraft.world.level.EntityGetter
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.UUID


enum class SUMMON_BEHAVIOURS {
    SENTRY, FOLLOWER, HUNTER, PLAYER_HUNTER, BERSERK
}

enum class SUMMON_PROPERTIES {
    RANGED, INVISIBLE
}

val MONSTER_HUNT_INDEXES = listOf(1, 5, 9)
val OTHER_PLAYER_HUNT_INDEXES = listOf(2, 6, 10)
val BERSERK_INDEXES = listOf(3, 7, 11)

fun calculateAiRoutineIndex(behaviours: List<SUMMON_BEHAVIOURS>?): Int {
    if (behaviours == null) {
        return 0
    }
    val targetVal = if (SUMMON_BEHAVIOURS.HUNTER in behaviours) {
        1
    } else if (SUMMON_BEHAVIOURS.PLAYER_HUNTER in behaviours) {
        2
    } else if (SUMMON_BEHAVIOURS.BERSERK in behaviours) {
        3
    } else {
        0
    }
    val movementVal = if (SUMMON_BEHAVIOURS.SENTRY in behaviours) {
        8
    } else if (SUMMON_BEHAVIOURS.FOLLOWER in behaviours) {
        4
    } else {
        0
    }
    return targetVal + movementVal
}

interface IMagicSummon {

//    0 - Roaming Mob, defends itself and the player
//    1 - Roaming Mob, hunts monsters
//    2 - Roaming Mob, hunts monsters and other players
//    3 - Roaming Mob, attacks everything
//    4 - Follower Mob, defends itself and the player
//    5 - Follower Mob, hunts monsters
//    6 - Follower Mob, hunts monsters and other players
//    7 - Follower Mob, attacks everything
//    8 - Sentry Mob, defends itself and the player
//    9 - Sentry Mob, hunts monsters
//    10 - Sentry Mob, hunts monsters and other players
//    11 - Sentry Mob, attacks everything
    var AI_ROUTINE_INDEX: Int?

    var summonerUUID: UUID?
    var color: Int
    var limitedLifeTicks: Int?

    var summonBoundOrigin: BlockPos?

    fun resetSummonBoundOrigin() {
        this.summonBoundOrigin = null
    }

    var summoner: LivingEntity?

    fun setAiRoutineIndex(index: Int?) {
        if (index == null) {
            this.AI_ROUTINE_INDEX = 0
        } else {
            this.AI_ROUTINE_INDEX = index
        }
    }

    fun level(): EntityGetter

    fun getSummonerAlt(): LivingEntity? {
//        if (this.summoner != null) {
//            if (this.level().players().size > 0) {
//                this.level().players().first().sendSystemMessage(Component.literal("Summoner id match " + (this.summoner!!.uuid == this.level().players().first().uuid).toString()))
//            }
//            return this.summoner
//        }
        val summonerUUID = this.summonerUUID
        if (summonerUUID != null && summonerUUID != Util.NIL_UUID) {
            this.summoner = this.level().getPlayerByUUID(summonerUUID)
            return this.summoner
        }
        return null
    }

    fun setSummonerAlt(summoner: LivingEntity?) {
        this.summoner = summoner
        if (this.summoner == null) {
            this.summonerUUID = null
        } else {
            this.summonerUUID = summoner!!.uuid
        }
    }

    fun isAlliedHelper(entity: Entity): Boolean {
        val summoner = getSummonerAlt()
        if (summoner == null) {
            return (entity is IMagicSummon && entity.color == this.color)
        }
        val hasCommonSummoner = (entity is IMagicSummon && entity.getSummonerAlt() == summoner)
        if (hasCommonSummoner) return true
        val isFellowAlly = entity == summoner || entity.isAlliedTo(summoner as Entity)
        val hasCommonOwner = (entity is OwnableEntity && entity.getOwner() == summoner)
        return isFellowAlly || hasCommonOwner
    }

    fun getSummonData(compoundTag: CompoundTag) {
        if (compoundTag.contains("lifetimeLimitedTicks")) {
            this.limitedLifeTicks = (compoundTag.getInt("lifetimeLimitedTicks"))
        }
        if (compoundTag.contains("AiRoutineIndex")) {
            this.AI_ROUTINE_INDEX = (compoundTag.getInt("AiRoutineIndex"))
        }
        if (compoundTag.contains("SummonerUUID")) {
            val uuid = compoundTag.getUUID("SummonerUUID")
            if (uuid != null) {
                this.summonerUUID = compoundTag.getUUID("SummonerUUID")
            }
        }
        if (compoundTag.contains("SummonBoundX")) {
            this.summonBoundOrigin =
                BlockPos(compoundTag.getInt("SummonBoundX"), compoundTag.getInt("SummonBoundY"), compoundTag.getInt("SummonBoundZ"))
        }
    }

    fun setSummonData(compoundTag: CompoundTag) {

        val limitedTicks = this.limitedLifeTicks
        if (limitedTicks != null) {
            compoundTag.putInt("lifetimeLimitedTicks", limitedTicks)
        }

        val aiRoutineIndex = this.AI_ROUTINE_INDEX
        if (aiRoutineIndex != null) {
            compoundTag.putInt("AiRoutineIndex", aiRoutineIndex)
        }
        if (this.summoner != null) {
            compoundTag.putUUID("SummonerUUID", this.summoner!!.uuid)
        } else {
            if (this.summonerUUID == null || this.summonerUUID == Util.NIL_UUID) {
                compoundTag.putUUID("SummonerUUID", Util.NIL_UUID)
            } else {
                compoundTag.putUUID("SummonerUUID", this.summonerUUID!!)
            }
        }
        if (this.summonBoundOrigin != null) {
            compoundTag.putInt("SummonBoundX", this.summonBoundOrigin!!.x)
            compoundTag.putInt("SummonBoundY", this.summonBoundOrigin!!.y)
            compoundTag.putInt("SummonBoundZ", this.summonBoundOrigin!!.z)
        }
    }

    fun spawnPoof(world: ServerLevel, pos: BlockPos) {
        for (i in 0..9) {
            val d0 = pos.getX() + 0.5
            val d1 = pos.getY() + 1.2
            val d2 = pos.getZ() + .5
            (world).sendParticles<SimpleParticleType?>(
                ParticleTypes.END_ROD,
                d0,
                d1,
                d2,
                2,
                (world.random.nextFloat() * 1 - 0.5) / 3,
                (world.random.nextFloat() * 1 - 0.5) / 3,
                (world.random.nextFloat() * 1 - 0.5) / 3,
                0.1
            )
        }
    }
}