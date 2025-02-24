package me.greysilly7.npcsmadeasy;

import com.github.juliarn.npclib.api.Npc;
import com.github.juliarn.npclib.api.Platform;
import com.github.juliarn.npclib.api.Position;
import com.github.juliarn.npclib.api.event.InteractNpcEvent;
import com.github.juliarn.npclib.api.event.ShowNpcEvent;
import com.github.juliarn.npclib.api.profile.Profile;
import com.github.juliarn.npclib.fabric.FabricPlatform;
import me.greysilly7.npcsmadeasy.config.Config;
import me.greysilly7.npcsmadeasy.config.NPC;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class NPCMadeEasyMod implements ModInitializer {
    public static final String MOD_ID = "npcs_made_easy";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final @NotNull Platform<ServerWorld, ServerPlayerEntity, ItemStack, ?> platform = FabricPlatform
            .fabricNpcPlatformBuilder()
            .extension(this)
            .actionController(builder -> {
            })
            .build();

    @Override
    public void onInitialize() {
        Path configPath = getConfigPath();

        Config config = new Config();
        try {
            config.loadConfig(configPath);
        } catch (Exception e) {
            LOGGER.error("Failed to load config from {}: {}", configPath, e.getMessage());
            return;
        }

        registerPayloads();
        registerWorldLoadListener(config);
    }

    private Path getConfigPath() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        return configDir.resolve("npcs_made_easy.json");
    }

    private void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(ServerSwitchPayload.ID, ServerSwitchPayload.CODEC);
    }

    private void registerWorldLoadListener(Config config) {
        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.getRegistryKey() == World.OVERWORLD) {
                spawnNpcs(config, world);
                registerNPCListeners(config);
            }
        });
    }

    private void spawnNpcs(Config config, ServerWorld world) {
        for (NPC npc : config.getNpcs()) {
            LOGGER.info("Spawning NPC {}", npc.name());
            platform.newNpcBuilder()
                    .position(Position.position(npc.position().x(), npc.position().y(), npc.position().z(),
                            world.getRegistryKey().getValue().toString()))
                    .profile(Profile.unresolved(npc.name()))
                    .thenAccept(builder -> {
                        if (builder instanceof Npc.Builder) {
                            ((Npc.Builder<ServerWorld, ServerPlayerEntity, ItemStack, ?>) builder).buildAndTrack();
                        }
                    });
        }
    }

    private void registerNPCListeners(Config config) {
        var eventManager = this.platform.eventManager();
        eventManager.registerEventHandler(InteractNpcEvent.class,
                interactNpcEvent -> handleNpcInteraction(interactNpcEvent, config));
        eventManager.registerEventHandler(ShowNpcEvent.Post.class, showEvent -> handleNpcShow(showEvent, config));
    }

    private void handleNpcInteraction(InteractNpcEvent interactNpcEvent, Config config) {
        var npc = interactNpcEvent.npc();
        ServerPlayerEntity player = interactNpcEvent.player();
        config.getNpcByName(npc.profile().name()).ifPresent(npcConfig -> {
            String server = npcConfig.serverToFowardPlayerTo();
            ServerPlayNetworking.send(player, new ServerSwitchPayload("Connect", server));
        });
    }

    private void handleNpcShow(ShowNpcEvent.Post showEvent, Config config) {
        var npc = showEvent.npc();
        config.getNpcByName(npc.profile().name()).ifPresent(npcConfig -> {
            npc.rotate(npcConfig.yaw(), npcConfig.pitch());
        });
    }
}