package com.ssblur.scriptor.data.saved_data

import com.ssblur.scriptor.mixin.DimensionDataStorageAccessor
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.phys.Vec3
import java.io.IOException
import java.nio.file.Files
import java.util.*

class TeleportWaypointSavedData: SavedData {
  var coordinate: Vec3? = null

  constructor(coordinate: Vec3?) {
    this.coordinate = coordinate
  }

  constructor()

  fun getWaypoint(): Vec3? {
    return this.coordinate
  }

  override fun save(tag: CompoundTag, provider: HolderLookup.Provider): CompoundTag {
    Vec3.CODEC.encodeStart(NbtOps.INSTANCE, this.coordinate).ifSuccess { tag.put("scriptor:teleport_waypoint", it) }
    return tag
  }

  companion object {
    fun load(tag: CompoundTag, @Suppress("unused_parameter") provider: HolderLookup.Provider?): TeleportWaypointSavedData? {
      val input = tag["scriptor:teleport_waypoint"]
      if (input != null) {
        val result = Vec3.CODEC.decode(NbtOps.INSTANCE, input).result()
        if (result.isPresent && result.get().first != null) return TeleportWaypointSavedData(result.get().first)
      }
      return null
    }
    /**
     * A helper for calling computeIfAbsent for this class from the Overworld
     * @param player Any ServerPlayer
     * @return The TeleportWaypointSavedData for this player
     */
    @JvmStatic
    fun computeIfAbsent(player: Player): TeleportWaypointSavedData? {
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
          { TeleportWaypointSavedData() },
          { tag: CompoundTag, provider: HolderLookup.Provider? -> load(tag, provider) },
          DataFixTypes.SAVED_DATA_MAP_DATA
        ),
        "scriptor_players/teleport_waypoint_" + player.stringUUID
      )
    }
  }
}
