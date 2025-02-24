package me.greysilly7.npcsmadeasy.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.greysilly7.npcsmadeasy.NPCMadeEasyMod;
import me.greysilly7.npcsmadeasy.config.NPC;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private List<NPC> npcs;

    // Load configuration from JSON file
    public void loadConfig(Path configPath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Check if the config file exists
        if (Files.notExists(configPath)) {
            // Create default config
            npcs = createDefaultNpcs();
            saveConfig(configPath); // Save the default config
        } else {
            // Load existing config
            try (FileReader reader = new FileReader(configPath.toFile())) {
                Type npcListType = new TypeToken<List<NPC>>() {}.getType();
                npcs = gson.fromJson(reader, npcListType);
            } catch (IOException e) {
                NPCMadeEasyMod.LOGGER.error(e.toString());
            }
        }
    }

    // Save configuration to JSON file
    public void saveConfig(Path configPath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(configPath.toFile())) {
            gson.toJson(npcs, writer);
        } catch (IOException e) {
            NPCMadeEasyMod.LOGGER.error(e.toString());
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
    public NPC getNpcByName(String name) {
        for (NPC npc : npcs) {
            if (npc.getName().equalsIgnoreCase(name)) {
                return npc;
            }
        }
        return null; // Return null if not found
    }
}
