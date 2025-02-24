package me.greysilly7.npcsmadeasy;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ServerSwitchPayload(byte[] bytes) implements CustomPayload {

    public static final CustomPayload.Id<ServerSwitchPayload> ID = new CustomPayload.Id<>(
            Identifier.tryParse("bungeecord:main"));

    public static final PacketCodec<PacketByteBuf, ServerSwitchPayload> CODEC = PacketCodec
            .of((value, buf) -> writeBytes(buf, value.bytes), ServerSwitchPayload::new);

    public ServerSwitchPayload(PacketByteBuf buf) {
        this(getWrittenBytes(buf));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private static byte[] getWrittenBytes(PacketByteBuf buf) {
        byte[] bs = new byte[buf.readableBytes()];
        buf.readBytes(bs);
        return bs;
    }

    private static void writeBytes(PacketByteBuf buf, byte[] v) {
        buf.writeBytes(v);
    }
}