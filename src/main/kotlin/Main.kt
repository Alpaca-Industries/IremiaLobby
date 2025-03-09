package org.alpacaindustries

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.extras.velocity.VelocityProxy
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.anvil.AnvilLoader
import org.alpacaindustries.config.Config
import org.alpacaindustries.config.ConfigLoader

object MinestormServer {

    private val config: Config =
            try {
                ConfigLoader.loadConfig()
            } catch (e: IOException) {
                throw ExceptionInInitializerError(e)
            }

    lateinit var instanceContainer: InstanceContainer
        private set

    @JvmStatic
    @Throws(IOException::class)
    fun main(args: Array<String>) {
        // Initialization
        val minecraftServer = MinecraftServer.init()

        // Create the instance
        val instanceManager = MinecraftServer.getInstanceManager()
        instanceContainer = instanceManager.createInstanceContainer()
        val worldPath = Path.of("./world")
        if (!Files.exists(worldPath)) {
            Files.createDirectories(worldPath)
        }
        instanceContainer.setChunkLoader(AnvilLoader(worldPath.resolve("world")))

        // Set up event handlers
        setupEventHandlers()

        // Add shutdown hook
        addShutdownHook()

        // Initialize Velocity if enabled
        initializeVelocity()

        val minigame = HousingMinigame(instanceContainer)

        // Start the server
        minecraftServer.start(config.host, config.port)
    }

    private fun setupEventHandlers() {
        val globalEventHandler = MinecraftServer.getGlobalEventHandler()
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
            val player: Player = event.player
            event.spawningInstance = instanceContainer

            // Use found spawn point or default if not yet available
            player.respawnPoint = Pos(0.0, 42.0, 0.0)

            player.gameMode = GameMode.ADVENTURE
        }
    }

    private fun addShutdownHook() {
        Runtime.getRuntime()
                .addShutdownHook(
                        Thread {
                            println("Shutting down server...")
                            MinecraftServer.stopCleanly()
                        }
                )
    }

    private fun initializeVelocity() {
        if (config.velocityEnabled) {
            VelocityProxy.enable(config.velocityForwardingSecret)
        }
        if (config.velocitySigned) {
            throw Exception("Velocity signed forwarding is not yet supported")
            // Additional logic for signed velocity
        }
    }
}
