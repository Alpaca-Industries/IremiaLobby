package org.alpacaindustries

import java.util.LinkedList
import java.util.Queue
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.block.Block

class HousingMinigame(private val instance: InstanceContainer) {

  private val spawnPoints: Queue<Pos> = LinkedList()
  private val players: MutableList<Player> = mutableListOf()
  private val eventManager: EventManager = EventManager(this)

  init {
    setupPlatforms()
    setupEvents()
  }

  private fun setupPlatforms() {
    val platformSize = 5
    val spacing = 10

    for (x in 0 until 5) {
      for (z in 0 until 5) {
        val startX = x * spacing
        val startZ = z * spacing

        // Create platforms
        for (px in 0 until platformSize) {
          for (pz in 0 until platformSize) {
            instance.setBlock(startX + px, 50, startZ + pz, Block.STONE)
          }
        }
        spawnPoints.add(Pos(startX + 2.0, 51.0, startZ + 2.0))
      }
    }
  }

  private fun setupEvents() {
    val eventNode = EventNode.type("player-events", EventFilter.PLAYER)

    eventNode.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
      val player = event.player
      event.spawningInstance = instance
      players.add(player)
    }

    eventNode.addListener(PlayerSpawnEvent::class.java) { event ->
      val player = event.player
      val spawnPos = spawnPoints.poll()
      player.sendMessage("§6Welcome to Horrific Housing Minestom Edition!")
      spawnPos?.let {
        player.teleport(it)
        player.sendMessage("§aYou have spawned on your floating platform!")
      }
    }

    MinecraftServer.getGlobalEventHandler().addChild(eventNode)
  }

  fun getPlayers(): List<Player> = players

  fun getInstance(): Instance = instance
}
