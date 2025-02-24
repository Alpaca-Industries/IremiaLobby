package me.greysilly7.npcsmadeasy;

import com.github.juliarn.npclib.api.Npc;
import com.github.juliarn.npclib.api.Platform;
import com.github.juliarn.npclib.api.Position;
import com.github.juliarn.npclib.api.event.InteractNpcEvent;
import com.github.juliarn.npclib.api.event.ShowNpcEvent;
import com.github.juliarn.npclib.api.profile.Profile;
import com.github.juliarn.npclib.fabric.FabricPlatform;
import com.github.juliarn.npclib.fabric.mixins.ServerGamePacketListenerImplMixin;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.buffer.Unpooled;
import me.greysilly7.npcsmadeasy.config.Config;
import me.greysilly7.npcsmadeasy.config.NPC;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

public class NPCMadeEasyMod implements ModInitializer {
    public static final String MOD_ID = "npcs_made_easy";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final @NotNull Platform platform = FabricPlatform.fabricNpcPlatformBuilder()
            .extension(this)
            .actionController(builder -> {
            })
            .build();

    @Override
    public void onInitialize() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configPath = configDir.resolve("npcs_made_easy.json");

        Config config = new Config();
        try {
            config.loadConfig(configPath);
        } catch (Exception e) {
            LOGGER.error("Failed to load config: {}", e.getMessage());
            return;
        }

        PayloadTypeRegistry.playS2C().register(ServerSwitchPayload.ID, ServerSwitchPayload.CODEC);

        // Register an event listener for when a world is loaded
        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.getRegistryKey() == World.OVERWORLD) {
                this.spawnNpcs(config, world);
                this.registerNPCListeners(config);
            }
        });
    }

    private void spawnNpcs(Config config, ServerWorld world) {
        for (NPC npc : config.getNpcs()) {
            LOGGER.info("Spawning NPC {}", npc.getName());
            platform.newNpcBuilder()
                    .position(Position.position(npc.getPosition().getX(),
                            npc.getPosition().getY(),
                            npc.getPosition().getZ(),
                            world.getRegistryKey().getValue().toString()))
                    .profile(Profile.unresolved(npc.getName()))
                    .thenAccept((builder -> {
                        if (builder instanceof Npc.Builder) {
                            ((Npc.Builder<ServerWorld, ServerPlayerEntity, ItemStack, ?>) builder).buildAndTrack();
                        }
                    }));
        }
    }

    private void registerNPCListeners(Config config) {
        var eventManager = this.platform.eventManager();
        eventManager.registerEventHandler(InteractNpcEvent.class, interactNpcEvent -> {
            var npc = interactNpcEvent.npc();
            ServerPlayerEntity player = interactNpcEvent.player();
            String server = config.getNpcByName(npc.profile().name()).getServerToFowardPlayerTo();

            ServerPlayNetworking.send(player, new ServerSwitchPayload("Connect", server));
        });

        eventManager.registerEventHandler(ShowNpcEvent.Post.class, showEvent -> {
            var npc = showEvent.npc();

            NPC npcConfig = config.getNpcByName(npc.profile().name());

            npc.rotate(npcConfig.getYaw(), npcConfig.getPitch());
        });
    }
}
