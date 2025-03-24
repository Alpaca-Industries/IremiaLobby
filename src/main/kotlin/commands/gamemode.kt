package org.alpacaindustries.commands
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import java.util.UUID

class GamemodeCommand(private val opsList: List<UUID>) : Command("gamemode") {

    init {
        val gamemodeArgument = ArgumentType.Enum("gamemode", GameMode::class.java)

        addSyntax({ sender, context ->
            if (sender is Player) {
                if (opsList.contains(sender.uuid)) {
                    val gamemode = context.get(gamemodeArgument)
                    sender.gameMode = gamemode
                    sender.sendMessage("Your gamemode has been changed to ${gamemode.name}.")
                } else {
                    sender.sendMessage("You do not have permission to use this command.")
                }
            } else {
                sender.sendMessage("This command can only be used by players.")
            }
        }, gamemodeArgument)
    }
}
