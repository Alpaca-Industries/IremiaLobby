package me.greysilly7.npcsmadeasy;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ServerSwitchPayload(String command, String serverName) implements CustomPayload {
    public static final CustomPayload.Id<ServerSwitchPayload> ID = new CustomPayload.Id<>(Identifier.of("bungeecord", "main"));
    public static final PacketCodec<RegistryByteBuf, ServerSwitchPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, ServerSwitchPayload::command, PacketCodecs.STRING, ServerSwitchPayload::serverName, ServerSwitchPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
