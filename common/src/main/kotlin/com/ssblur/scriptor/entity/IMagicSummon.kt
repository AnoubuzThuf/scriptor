package com.ssblur.scriptor.entity

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.CombatTracker
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.OwnableEntity
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level

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
}