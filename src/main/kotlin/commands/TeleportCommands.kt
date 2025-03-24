package org.alpacaindustries.commands

import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import revxrsal.commands.annotation.Command
import java.util.UUID

class TeleportCommands(private val opsList: List<UUID>) {
    @Command("teleport", "tp")
    fun teleport(sender: Player, x: Double, y: Double, z: Double) {
        if (!opsList.contains(sender.uuid)) {
            sender.sendMessage("You do not have permission to use this command.")
            return
        }
        sender.teleport(Pos(x, y, z))
    }

    @Command("teleport", "tp")
    fun teleport(sender: Player, target: Player, x: Double, y: Double, z: Double) {
        if (!opsList.contains(sender.uuid)) {
            sender.sendMessage("You do not have permission to use this command.")
            return
        }
        target.teleport(Pos(x, y, z))
    }

    @Command("teleport <target> here")
    fun teleportHere(sender: Player, target: Player) {
        if (!opsList.contains(sender.uuid)) {
            sender.sendMessage("You do not have permission to use this command.")
            return
        }

        target.teleport(sender.position)
    }

    @Command("teleport", "tp")
    fun teleport(sender: Player, target: Player) {
        if (!opsList.contains(sender.uuid)) {
            sender.sendMessage("You do not have permission to use this command.")
            return
        }

        sender.teleport(target.position)
    }
}
