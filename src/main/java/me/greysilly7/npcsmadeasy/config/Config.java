package me.greysilly7.npcsmadeasy.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.greysilly7.npcsmadeasy.NPCMadeEasyMod;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type NPC_LIST_TYPE = new TypeToken<List<NPC>>() {
    }.getType();
    private List<NPC> npcs;

    // Load configuration from JSON file
    public void loadConfig(Path configPath) {
        if (Files.notExists(configPath)) {
            createAndSaveDefaultConfig(configPath);
        } else {
            loadExistingConfig(configPath);
        }
    }

    private void createAndSaveDefaultConfig(Path configPath) {
        npcs = createDefaultNpcs();
        saveConfig(configPath);
    }

    private void loadExistingConfig(Path configPath) {
        try (FileReader reader = new FileReader(configPath.toFile())) {
            npcs = GSON.fromJson(reader, NPC_LIST_TYPE);
        } catch (IOException e) {
            NPCMadeEasyMod.LOGGER.error("Failed to load config from {}: {}", configPath, e.getMessage());
        }
    }

    // Save configuration to JSON file
    public void saveConfig(Path configPath) {
        try (FileWriter writer = new FileWriter(configPath.toFile())) {
            GSON.toJson(npcs, writer);
        } catch (IOException e) {
            NPCMadeEasyMod.LOGGER.error("Failed to save config to {}: {}", configPath, e.getMessage());
        }
    }

    // Create default NPCs
    private List<NPC> createDefaultNpcs() {
        List<NPC> defaultNpcs = new ArrayList<>();
        defaultNpcs.add(new NPC("Guard", new NPC.Position(10, 2, 5), "survival", 0, 90));
        defaultNpcs.add(new NPC("Merchant", new NPC.Position(25, 0, 12), "survival", 0, 90));
        defaultNpcs.add(new NPC("Healer", new NPC.Position(5, 1, 8), "survivals", 0, 90));
        return defaultNpcs;
    }

    // Fetch all NPCs
    public List<NPC> getNpcs() {
        return npcs;
    }

    // Fetch a specific NPC by name
    public Optional<NPC> getNpcByName(String name) {
        return npcs.stream()
                .filter(npc -> npc.name().equalsIgnoreCase(name))
                .findFirst();
    }
}