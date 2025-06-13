package com.ssblur.scriptor.data.saved_data

import com.mojang.serialization.Codec
import com.ssblur.scriptor.mixin.DimensionDataStorageAccessor
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import java.io.IOException
import java.nio.file.Files
import java.util.*

class LastCastSpellSavedData: SavedData {
  var spell_text: String? = null

  constructor(spell: String?) {
    this.spell_text = spell
  }

  constructor()

  fun getSpell(): String? {
    return this.spell_text
  }

  override fun save(tag: CompoundTag, provider: HolderLookup.Provider): CompoundTag {
    Codec.STRING.encodeStart(NbtOps.INSTANCE, this.spell_text).ifSuccess { tag.put("scriptor:last_cast_spell", it) }
    return tag
  }

  companion object {
    fun load(tag: CompoundTag, @Suppress("unused_parameter") provider: HolderLookup.Provider?): LastCastSpellSavedData? {
      val input = tag["scriptor:last_cast_spell"]
      if (input != null) {
        val result = Codec.STRING.decode(NbtOps.INSTANCE, input).result()
        if (result.isPresent && result.get().first != null) return LastCastSpellSavedData(result.get().first)
      }
      return null
    }
    /**
     * A helper for calling computeIfAbsent for this class from the Overworld
     * @param player Any ServerPlayer
     * @return The TeleportWaypointSavedData for this player
     */
    @JvmStatic
    fun computeIfAbsent(player: Player): LastCastSpellSavedData? {
      val level = player.level()
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
          { LastCastSpellSavedData() },
          { tag: CompoundTag, provider: HolderLookup.Provider? -> load(tag, provider) },
          DataFixTypes.SAVED_DATA_MAP_DATA
        ),
        "scriptor_players/last_cast_spell_" + player.stringUUID
      )
    }
  }
}
