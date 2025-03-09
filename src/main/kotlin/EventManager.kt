package org.alpacaindustries

import java.util.*
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.instance.block.Block
import net.minestom.server.timer.TaskSchedule

class EventManager(private val game: HousingMinigame) {

  private val random = Random()
  private var infectedPlayer: Player? = null

  init {
    startEventScheduler()
  }

  private fun startEventScheduler() {
    MinecraftServer.getSchedulerManager().submitTask {
      triggerRandomEvent()
      TaskSchedule.seconds(30) // New event every 30 seconds
    }
  }

  private fun triggerRandomEvent() {
    when (random.nextInt(3)) {
      0 -> spawnZombieOnRandomPlatform()
      1 -> removeRandomPlatform()
      2 -> startInfectionEvent()
    }
  }

  private fun spawnZombieOnRandomPlatform() {
    val players = game.getPlayers()
    if (players.isEmpty()) return

    val target = players[random.nextInt(players.size)]
    target.sendMessage("§cA zombie has spawned on your platform! Run!")

    // Spawn zombie (example, you'd need an actual EntityZombie)
  }

  private fun removeRandomPlatform() {
    val players = game.getPlayers()
    if (players.isEmpty()) return

    val unluckyPlayer = players[random.nextInt(players.size)]
    val playerPos = unluckyPlayer.position

    // Remove the platform below them
    for (x in -2..2) {
      for (z in -2..2) {
        game.getInstance().setBlock(playerPos.blockX() + x, 50, playerPos.blockZ() + z, Block.AIR)
      }
    }
    unluckyPlayer.sendMessage("§4Your platform has disappeared! Jump to survive!")
  }

  private fun startInfectionEvent() {
    val players = game.getPlayers()
    if (players.isEmpty()) return

    infectedPlayer =
            players[random.nextInt(players.size)].apply {
              sendMessage("§5You are infected! Tag others to spread the infection!")
              displayName = Component.text("§c[INFECTED] $username")
            }

    MinecraftServer.getSchedulerManager().submitTask {
      spreadInfection()
      TaskSchedule.tick(10)
    }
  }

  private fun spreadInfection() {
    val infected = infectedPlayer ?: return

    game.getPlayers().forEach { player ->
      if (player != infected && isNear(infected, player)) {
        player.sendMessage("§cYou've been infected! Now tag others!")
        player.displayName = Component.text("§c[INFECTED] ${player.username}")
        infectedPlayer = player
      }
    }
  }

  private fun isNear(p1: Player, p2: Player): Boolean {
    return p1.position.distance(p2.position) < 2
  }
}
