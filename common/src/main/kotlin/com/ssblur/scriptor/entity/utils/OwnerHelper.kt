package com.ssblur.scriptor.entity.utils

import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import java.util.UUID

fun getAndCacheOwner(level: Level, owner: LivingEntity?, ownerUUID: UUID?): LivingEntity? {
    if ((owner != null) && (owner.isAlive())) {
        return owner
    } else if (ownerUUID != null && level is ServerLevel) {
        if (level.getEntity(ownerUUID) is LivingEntity) {
            return level.getEntity(ownerUUID) as LivingEntity
        }
        return owner
    }
    return null
}

fun serializeOwner(compoundTag: CompoundTag, ownerUUID: UUID?) {
    if (ownerUUID != null) {
        compoundTag.putUUID("summoner", ownerUUID)
    }
}

fun deserializeOwner(compoundTag: CompoundTag): UUID? {
    if (compoundTag.hasUUID("summoner")) {
        return compoundTag.getUUID("summoner")
    }
    return null
}