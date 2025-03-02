package org.alpacaindustries.lobbyServer;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.EventFilter;
import net.minestom.server.MinecraftServer;

import java.util.*;

public class HousingMinigame {
  private final InstanceContainer instance;
  private final Queue<Pos> spawnPoints = new LinkedList<>();
  private final List<Player> players = new ArrayList<>();
  private final EventManager eventManager;

  public HousingMinigame(InstanceContainer instanceContainer) {
    this.instance = instanceContainer;
    this.instance.setGenerator(new FlatWorldGenerator());
    this.eventManager = new EventManager(this);

    setupPlatforms();
    setupEvents();
  }

  private void setupPlatforms() {
    int platformSize = 5;
    int spacing = 10;

    for (int x = 0; x < 5; x++) {
      for (int z = 0; z < 5; z++) {
        int startX = x * spacing;
        int startZ = z * spacing;

        // Create platforms
        for (int px = 0; px < platformSize; px++) {
          for (int pz = 0; pz < platformSize; pz++) {
            instance.setBlock(startX + px, 50, startZ + pz, Block.STONE);
          }
        }
        spawnPoints.add(new Pos(startX + 2, 51, startZ + 2));
      }
    }
  }

  private void setupEvents() {
    EventNode<PlayerEvent> eventNode = EventNode.type("player-events", EventFilter.PLAYER);

    eventNode.addListener(EventListener.builder(AsyncPlayerConfigurationEvent.class)
        .handler(event -> {
          Player player = event.getPlayer();
          event.setSpawningInstance(instance);
          players.add(player);
        })
        .build());

    eventNode.addListener(EventListener.builder(PlayerSpawnEvent.class)
        .handler(event -> {
          Player player = event.getPlayer();
          Pos spawnPos = spawnPoints.poll();
          player.sendMessage("§6Welcome to Horrific Housing Minestom Edition!");
          if (spawnPos != null)
            player.teleport(spawnPos);
          player.sendMessage("§aYou have spawned on your floating platform!");
        })
        .build());

    MinecraftServer.getGlobalEventHandler().addChild(eventNode);
  }

  public List<Player> getPlayers() {
    return players;
  }

  public Instance getInstance() {
    return instance;
  }
}
