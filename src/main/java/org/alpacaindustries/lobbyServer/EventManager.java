package org.alpacaindustries.lobbyServer;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;

import java.util.List;
import java.util.Random;

public class EventManager {
  private final HousingMinigame game;
  private final Random random = new Random();
  private Player infectedPlayer;

  public EventManager(HousingMinigame game) {
    this.game = game;
    startEventScheduler();
  }

  private void startEventScheduler() {
    MinecraftServer.getSchedulerManager().submitTask(() -> {
      triggerRandomEvent();
      return TaskSchedule.seconds(30); // New event every 30 seconds
    });
  }

  private void triggerRandomEvent() {
    int eventType = random.nextInt(3);

    switch (eventType) {
      case 0:
        spawnZombieOnRandomPlatform();
        break;
      case 1:
        removeRandomPlatform();
        break;
      case 2:
        startInfectionEvent();
        break;
    }
  }

  private void spawnZombieOnRandomPlatform() {
    List<Player> players = game.getPlayers();
    if (players.isEmpty())
      return;

    Player target = players.get(random.nextInt(players.size()));
    target.sendMessage("§cA zombie has spawned on your platform! Run!");

    // Spawn zombie (example, you'd need an actual EntityZombie)
  }

  private void removeRandomPlatform() {
    List<Player> players = game.getPlayers();
    if (players.isEmpty())
      return;

    Player unluckyPlayer = players.get(random.nextInt(players.size()));
    Pos playerPos = unluckyPlayer.getPosition();

    // Remove the platform below them
    for (int x = -2; x <= 2; x++) {
      for (int z = -2; z <= 2; z++) {
        game.getInstance().setBlock(playerPos.blockX() + x, 50, playerPos.blockZ() + z, Block.AIR);
      }
    }
    unluckyPlayer.sendMessage("§4Your platform has disappeared! Jump to survive!");
  }

  private void startInfectionEvent() {
    List<Player> players = game.getPlayers();
    if (players.isEmpty())
      return;

    infectedPlayer = players.get(random.nextInt(players.size()));
    infectedPlayer.sendMessage("§5You are infected! Tag others to spread the infection!");
    infectedPlayer.setDisplayName(Component.text("§c[INFECTED] " + infectedPlayer.getUsername()));

    MinecraftServer.getSchedulerManager().submitTask(() -> {
      spreadInfection();
      return TaskSchedule.tick(10);
    });
  }

  private void spreadInfection() {
    if (infectedPlayer == null)
      return;

    for (Player player : game.getPlayers()) {
      if (player != infectedPlayer && isNear(infectedPlayer, player)) {
        player.sendMessage("§cYou've been infected! Now tag others!");
        player.setDisplayName(Component.text("§c[INFECTED] " + player.getUsername()));
        infectedPlayer = player;
      }
    }
  }

  private boolean isNear(Player p1, Player p2) {
    return p1.getPosition().distance(p2.getPosition()) < 2;
  }
}
