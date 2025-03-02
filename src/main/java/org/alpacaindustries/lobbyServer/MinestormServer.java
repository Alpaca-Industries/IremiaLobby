package org.alpacaindustries.lobbyServer;

import java.io.IOException;
import java.nio.file.Path;

import org.alpacaindustries.lobbyServer.config.Config;
import org.alpacaindustries.lobbyServer.config.ConfigLoader;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.anvil.AnvilLoader;

public class MinestormServer {

  private static final Config CONFIG;
  public static InstanceContainer instanceContainer;

  static {
    try {
      CONFIG = ConfigLoader.loadConfig();
    } catch (IOException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static void main(String[] args) throws IOException {
    // Initialization
    MinecraftServer minecraftServer = MinecraftServer.init();

    // Create the instance
    InstanceManager instanceManager = MinecraftServer.getInstanceManager();
    instanceContainer = instanceManager.createInstanceContainer();
    if (!Path.of("./world").toFile().exists())
      Path.of("./world").toFile().mkdirs();
    instanceContainer.setChunkLoader(new AnvilLoader(Path.of("./world/world")));

    // Set up event handlers
    setupEventHandlers(instanceContainer);

    // Add shutdown hook
    addShutdownHook(instanceContainer);

    // Initialize Velocity if enabled
    initializeVelocity();

    HousingMinigame minigame = new HousingMinigame(instanceContainer);

    // Start the server
    minecraftServer.start(CONFIG.host(), CONFIG.port());
  }

  private static void setupEventHandlers(InstanceContainer instanceContainer) {
    GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
    globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
      final Player player = event.getPlayer();
      event.setSpawningInstance(instanceContainer);

      // Use found spawn point or default if not yet available
      player.setRespawnPoint(new Pos(0, 42, 0));

      player.setGameMode(GameMode.ADVENTURE);
    });
  }

  private static void addShutdownHook(InstanceContainer instanceContainer) {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("Shutting down server...");
      MinecraftServer.stopCleanly();
    }));
  }

  private static void initializeVelocity() {
    if (CONFIG.velocityEnabled()) {
      VelocityProxy.enable(CONFIG.velocityForwardingSecret());
    }
    if (CONFIG.velocitySigned()) {

    }
  }
}