package org.alpacaindustries

import com.github.juliarn.npclib.api.Npc
import com.github.juliarn.npclib.api.Position
import com.github.juliarn.npclib.api.event.InteractNpcEvent
import com.github.juliarn.npclib.api.profile.Profile
import com.github.juliarn.npclib.api.profile.ProfileProperty
import com.github.juliarn.npclib.minestom.MinestomPlatform
import dev.lu15.voicechat.VoiceChat
import io.github.oshai.kotlinlogging.KotlinLogging
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.Damage
import net.minestom.server.event.entity.EntityAttackEvent
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.event.player.PlayerMoveEvent
import net.minestom.server.event.server.ServerListPingEvent
import net.minestom.server.extras.velocity.VelocityProxy
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.anvil.AnvilLoader
import net.minestom.server.item.ItemStack
import net.minestom.server.timer.Task
import net.minestom.server.timer.TaskSchedule
import org.alpacaindustries.commands.GamemodeCommand
import org.alpacaindustries.commands.TeleportCommands
import org.alpacaindustries.config.Config
import org.alpacaindustries.config.ConfigLoader
import revxrsal.commands.minestom.MinestomLamp
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

object MinestormServer {
    private val logger = KotlinLogging.logger {}

    val config: Config =
            runCatching { ConfigLoader.loadConfig() }.getOrElse {
                throw ExceptionInInitializerError(it)
            }

    private lateinit var instanceContainer: InstanceContainer

    private val platform by lazy {
        MinestomPlatform.minestomNpcPlatformBuilder().extension(this).actionController {}.build()
    }

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
        instanceContainer.chunkLoader = AnvilLoader(worldPath)
        instanceContainer.setChunkSupplier(::LightingChunk)

        // We hate daylight cycle
        instanceContainer.setTimeRate(0)

        // Set up event handlers
        setupEventHandlers()

        // Add shutdown hook
        addShutdownHook()

        // Initialize Velocity if enabled
        initializeVelocity()

        spawnNPCS()
        addNPCEvents()
        parkourTimer()

        val lamp = MinestomLamp.builder().build()
        MinecraftServer.getCommandManager().register(GamemodeCommand(config.ops))
        lamp.register(TeleportCommands(config.ops))



        // Start the server
        logger.info { "Starting server on ${config.host}:${config.port}" }
        logger.info { "Starting Simple Voice Chat server on ${config.host}:${config.svcPort}" }
        VoiceChat.builder(config.host, config.svcPort).enable()
        minecraftServer.start(config.host, config.port)
    }

    private fun setupEventHandlers() {
        val globalEventHandler = MinecraftServer.getGlobalEventHandler()
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
            val player: Player = event.player
            event.spawningInstance = instanceContainer

            player.respawnPoint = Pos(0.0, 17.0, 0.0)

            player.gameMode = GameMode.ADVENTURE
        }

        val attackCooldowns = mutableMapOf<UUID, Long>()
        val attackStacks = mutableMapOf<UUID, Int>()
        val cooldownTime = 1000L // 1 second cooldown
        val maxStacks = 100

        globalEventHandler.addListener(EntityAttackEvent::class.java) { event ->
            val victim = event.target as? Player ?: return@addListener
            val attacker = event.entity as? Player ?: return@addListener
            val currentTime = System.currentTimeMillis()

            val lastAttackTime = attackCooldowns[attacker.uuid] ?: 0L
            if (currentTime - lastAttackTime < cooldownTime) {
                return@addListener
            }

            attackCooldowns[attacker.uuid] = currentTime
            val currentStacks = attackStacks.getOrDefault(attacker.uuid, 0) + 1
            attackStacks[attacker.uuid] = currentStacks.coerceAtMost(maxStacks)

            MinecraftServer.getConnectionManager().onlinePlayers.forEach { onlinePlayer ->
                onlinePlayer.sendMessage(
                        "${attacker.username} hit ${victim.username} with stack $currentStacks!"
                )
            }
            victim.damage(Damage.fromPlayer(attacker, 1.0f * currentStacks))
        }
        globalEventHandler.addListener(PlayerChatEvent::class.java) { event ->
            event.isCancelled = true
        }

        globalEventHandler.addListener(ServerListPingEvent::class.java) { event ->
            val onlinePlayers = MinecraftServer.getConnectionManager().onlinePlayerCount
            event.responseData.apply {
                online = onlinePlayers
                maxPlayer = (onlinePlayers + 10)
            }
        }
    }

    private fun spawnNPCS() {
        config.npcs.forEach { npc ->
            runCatching {
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
                logger.info { "Spawned NPC: ${npc.name} at (${npc.x}, ${npc.y}, ${npc.z})" }
            }
                    .onFailure { e ->
                        logger.error { "Failed to spawn NPC ${npc.name}: ${e.message}" }
                    }
        }
    }

    private fun addNPCEvents() {
        platform.eventManager().registerEventHandler(InteractNpcEvent::class.java) { event ->
            val player = event.player<Player>()
            val npcName = event.npc<Instance, Player, ItemStack, Any>().profile().name()

            // Find the NPC's target server
            config.npcs.find { it.name == npcName }?.let { npc ->
                try {
                    ByteArrayOutputStream().use { byteArrayStream ->
                        DataOutputStream(byteArrayStream).use { dataStream ->
                            dataStream.writeUTF("Connect")
                            dataStream.writeUTF(npc.server)

                            player.sendPluginMessage(
                                    "bungeecord:main",
                                    byteArrayStream.toByteArray()
                            )
                            logger.info {
                                "Sending player ${player.username} to server ${npc.server}"
                            }
                        }
                    }
                } catch (e: IOException) {
                    logger.error { "Failed to send player to server: ${e.message}" }
                }
            }
                    ?: logger.warn { "NPC with name $npcName not found in config" }
        }
    }

    private fun addShutdownHook() {
        val shutdownHook = Thread {
            logger.info { "Shutting down server..." }
            instanceContainer.saveChunksToStorage()
            MinecraftServer.stopCleanly()
        }

        Runtime.getRuntime().addShutdownHook(shutdownHook)

        // Handle Ctrl+C
        Runtime.getRuntime().addShutdownHook(Thread {
            if (shutdownHook.state != Thread.State.TERMINATED) {
                shutdownHook.join()
            }
        })
    }

    private fun initializeVelocity() {
        if (config.velocityEnabled) {
            VelocityProxy.enable(config.velocityForwardingSecret)
        }
        if (config.velocitySigned) {
            // Additional logic for signed velocity
        }
    }

    private fun parkourTimer() {
        val pressurePlatePos = Pos(0.0, 17.0, -25.0)
        val playerTimers = mutableMapOf<UUID, Long>()
        val activeTasks = mutableMapOf<UUID, Task>()

        val globalEventHandler = MinecraftServer.getGlobalEventHandler()
        globalEventHandler.addListener(PlayerMoveEvent::class.java) { event ->
            val player = event.player
            val playerPos = player.position
            val playerUUID = player.uuid

            if (playerPos.sameBlock(pressurePlatePos)) {
                val startTime = playerTimers.getOrPut(playerUUID) { System.currentTimeMillis() }

                // If no active task, create one
                if (!activeTasks.containsKey(playerUUID)) {
                    val task = player.scheduler().submitTask {
                        val elapsedTime = (System.currentTimeMillis() - startTime) / 1000
                        player.sendActionBar(Component.text("Time: $elapsedTime sec", NamedTextColor.GREEN))

                        // Cancel the task if the player drops below Y 16
                        if (player.position.y <= 16) {
                            playerTimers.remove(playerUUID)
                            activeTasks.remove(playerUUID)?.cancel()
                            player.sendActionBar(Component.empty())
                            TaskSchedule.stop()
                        } else {
                            TaskSchedule.seconds(1)
                        }
                    }
                    activeTasks[playerUUID] = task
                }
            }
        }
    }}
