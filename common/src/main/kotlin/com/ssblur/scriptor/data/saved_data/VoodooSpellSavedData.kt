package com.ssblur.scriptor.data.saved_data

import com.mojang.serialization.Codec
import com.ssblur.scriptor.mixin.DimensionDataStorageAccessor
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import java.io.IOException
import java.nio.file.Files
import java.util.*

fun safeStrToUuid(str_val: String?): UUID? {
  if (str_val == null) {
    return null
  }
  try {
    return UUID.fromString(str_val)
  } catch (e: IllegalArgumentException) {
    return null
  }
}

class VoodooSpellSavedData: SavedData {
  var voodooSubjectUuid: UUID? = null

  constructor(spell: UUID?) {
    this.voodooSubjectUuid = spell
  }

  constructor()

  fun getSubjectId(): UUID? {
    return this.voodooSubjectUuid
  }

  override fun save(tag: CompoundTag, provider: HolderLookup.Provider): CompoundTag {
    Codec.STRING.encodeStart(NbtOps.INSTANCE, this.voodooSubjectUuid.toString()).ifSuccess { tag.put("scriptor:voodoo_spell_victim", it) }
    return tag
  }

  companion object {
    fun load(tag: CompoundTag, @Suppress("unused_parameter") provider: HolderLookup.Provider?): VoodooSpellSavedData? {
      val input = tag["scriptor:voodoo_spell_victim"]
      if (input != null) {
        val result = Codec.STRING.decode(NbtOps.INSTANCE, input).result()
        if (result.isPresent && result.get().first != null) return VoodooSpellSavedData(safeStrToUuid(result.get().first))
      }
      return null
    }
    /**
     * A helper for calling computeIfAbsent for this class from the Overworld
     * @param entity Any LivingEntity
     * @return The TeleportWaypointSavedData for this player
     */
    @JvmStatic
    fun computeIfAbsent(entity: LivingEntity): VoodooSpellSavedData? {
      val level = entity.level()
      val minecraft = level.server ?: return null
      val server = minecraft.getLevel(Level.OVERWORLD) ?: return null

      try {
        val storage = server.dataStorage as DimensionDataStorageAccessor

        if (!Files.exists(storage.dataFolder.toPath().resolve("scriptor_players"))) Files.createDirectory(
          storage.dataFolder.toPath().resolve("scriptor_players")
        )
      } catch (e: IOException) {
        throw RuntimeException(e)
      }
      Objects.requireNonNull(server)
      return server.dataStorage.computeIfAbsent(
        Factory(
          { VoodooSpellSavedData() },
          { tag: CompoundTag, provider: HolderLookup.Provider? -> load(tag, provider) },
          DataFixTypes.SAVED_DATA_MAP_DATA
        ),
        "scriptor_players/voodoo_spell_victim_" + entity.stringUUID
      )
    }
  }
}
