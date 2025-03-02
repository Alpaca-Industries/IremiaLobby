package org.alpacaindustries.lobbyServer;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;

public class FlatWorldGenerator implements Generator {
  private static final int PLATFORM_SIZE = 5;
  private static final int SPACING = 10;
  private static final int BRIDGE_WIDTH = 2;
  private static final int PLATFORM_HEIGHT = 50; // Y-Level of platforms

  private static final int GRID_WIDTH = 4; // 4 platforms in one row
  private static final int GRID_HEIGHT = 3; // 3 rows of platforms

  @Override
  public void generate(GenerationUnit unit) {
    Point start = unit.absoluteStart();

    int baseX = start.blockX();
    int baseZ = start.blockZ();

    // Define the exact platform layout
    for (int row = 0; row < GRID_HEIGHT; row++) {
      for (int col = 0; col < GRID_WIDTH; col++) {
        int x = baseX + col * SPACING;
        int z = baseZ + row * SPACING;

        generatePlatform(unit, x, PLATFORM_HEIGHT, z);

        // Generate horizontal bridge (if not last column)
        if (col < GRID_WIDTH - 1) {
          generateBridge(unit, x, PLATFORM_HEIGHT, z, true);
        }

        // Generate vertical bridge (if not last row)
        if (row < GRID_HEIGHT - 1) {
          generateBridge(unit, x, PLATFORM_HEIGHT, z, false);
        }
      }
    }
  }

  private void generatePlatform(GenerationUnit unit, int startX, int startY, int startZ) {
    for (int x = 0; x < PLATFORM_SIZE; x++) {
      for (int z = 0; z < PLATFORM_SIZE; z++) {
        unit.modifier().setBlock(new Pos(startX + x, startY, startZ + z), Block.STONE);
      }
    }
  }

  private void generateBridge(GenerationUnit unit, int startX, int startY, int startZ, boolean horizontal) {
    int bridgeStartX = horizontal ? startX + PLATFORM_SIZE : startX + PLATFORM_SIZE / 2 - BRIDGE_WIDTH / 2;
    int bridgeStartZ = horizontal ? startZ + PLATFORM_SIZE / 2 - BRIDGE_WIDTH / 2 : startZ + PLATFORM_SIZE;

    for (int x = 0; x < (horizontal ? SPACING - PLATFORM_SIZE : BRIDGE_WIDTH); x++) {
      for (int z = 0; z < (horizontal ? BRIDGE_WIDTH : SPACING - PLATFORM_SIZE); z++) {
        unit.modifier().setBlock(new Pos(bridgeStartX + x, startY, bridgeStartZ + z), Block.STONE_SLAB);
      }
    }
  }
}
