package org.alpacaindustries

import com.github.juliarn.npclib.api.Npc
import com.github.juliarn.npclib.api.Position
import com.github.juliarn.npclib.api.profile.Profile
import com.github.juliarn.npclib.api.profile.ProfileProperty
import com.github.juliarn.npclib.minestom.MinestomPlatform
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.Damage
import net.minestom.server.event.entity.EntityAttackEvent
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.extras.velocity.VelocityProxy
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.anvil.AnvilLoader
import org.alpacaindustries.config.Config
import org.alpacaindustries.config.ConfigLoader

object MinestormServer {

    private val config: Config =
            runCatching { ConfigLoader.loadConfig() }.getOrElse {
                throw ExceptionInInitializerError(it)
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
        // instanceContainer.setChunkLoader(AnvilLoader(worldPath.resolve("world")))
        instanceContainer.setChunkLoader(AnvilLoader("/home/greysilly7/git/lobbyServer/world"))
        // Set up event handlers
        setupEventHandlers()

        // Add shutdown hook
        addShutdownHook()

        // Initialize Velocity if enabled
        initializeVelocity()

        spawnNPCS()

        // Start the server
        minecraftServer.start(config.host, config.port)
    }

    private fun setupEventHandlers() {
        val globalEventHandler = MinecraftServer.getGlobalEventHandler()
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
            val player: Player = event.player
            event.spawningInstance = instanceContainer

            // Use found spawn point or default if not yet available
            player.respawnPoint = Pos(0.0, 17.0, 0.0)

            player.gameMode = GameMode.SURVIVAL
        }

        globalEventHandler.addListener(EntityAttackEvent::class.java) { event ->
            val victim = event.target as? Player ?: return@addListener
            val attacker = event.entity as? Player ?: return@addListener

            attacker.sendMessage("${victim.username} §c got hit!")
            victim.damage(Damage.fromPlayer(attacker, 1.0f))
        }

        globalEventHandler.addListener(EntityDamageEvent::class.java) { event ->
            val victim = event.entity as? Player ?: return@addListener
            val attacker = event.damage.source as? Player ?: return@addListener
            attacker.sendMessage("${victim.username} §c got hit!")
            event.isCancelled = false
        }
    }

    private fun spawnNPCS() {
        val platform =
                MinestomPlatform.minestomNpcPlatformBuilder()
                        .extension(this)
                        .actionController({})
                        .build()

        config.npcs.forEach { npc ->
            platform.newNpcBuilder()
                    .flag(Npc.LOOK_AT_PLAYER, true)
                    .flag(Npc.HIT_WHEN_PLAYER_HITS, true)
                    .flag(Npc.SNEAK_WHEN_PLAYER_SNEAKS, true)
                    .position(
                            Position.position(
                                    npc.x,
                                    npc.y,
                                    npc.z,
                                    npc.yaw,
                                    npc.pitch,
                                    instanceContainer.uuid.toString()
                            )
                    )
                    .profile(
                            Profile.resolved(
                                    npc.name,
                                    UUID.randomUUID(),
                                    setOf(
                                            ProfileProperty.property(
                                                    "textures",
                                                    npc.skinValue,
                                                    npc.skinSignature
                                            )
                                    )
                            )
                    )
                    .buildAndTrack()
        }
    }

    private fun addNPCEvents() {}

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
