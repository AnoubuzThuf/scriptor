package com.ssblur.scriptor.entity

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.OwnableEntity
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level


enum class SUMMON_BEHAVIOURS {
    SENTRY, FOLLOWER, HUNTER, BERSERK
}

enum class SUMMON_PROPERTIES {
    RANGED, INVISIBLE
}

interface IMagicSummon {
    fun getSummoner(): LivingEntity?

    fun onDeathHelper() {
        if (this is LivingEntity) {
            val level: Level = this.level()
            var deathMessage = this.getCombatTracker().getDeathMessage()
            var deathMessageString = this.getCombatTracker().getDeathMessage().getString()
            if ("starved" in deathMessageString) {
//                Skip death message if the summon despawns normally
                return
            }
            if (!level.isClientSide && level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && getSummoner() != null && getSummoner() is ServerPlayer) {
                getSummoner()!!.sendSystemMessage(deathMessage)
            }
        }
    }

    fun isAlliedHelper(entity: Entity): Boolean {
        val summoner = getSummoner()
        if (summoner == null) {
            return false
        }
        val isFellowAlly = entity == summoner || entity.isAlliedTo(summoner as Entity)
        val hasCommonOwner = (entity is OwnableEntity && entity.getOwner() == summoner)
        return isFellowAlly || hasCommonOwner
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