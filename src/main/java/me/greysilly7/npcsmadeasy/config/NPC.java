package me.greysilly7.npcsmadeasy.config;

import com.mojang.serialization.Codec;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;

public record NPC(String name, Position position, String serverToFowardPlayerTo, float yaw, float pitch) {

    public static final Codec<NPC> CODEC = create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(NPC::name),
            Position.CODEC.fieldOf("position").forGetter(NPC::position),
            Codec.STRING.fieldOf("serverToFowardPlayerTo").forGetter(NPC::serverToFowardPlayerTo),
            Codec.FLOAT.fieldOf("yaw").forGetter(NPC::yaw),
            Codec.FLOAT.fieldOf("pitch").forGetter(NPC::pitch)).apply(instance, NPC::new));

    public record Position(double x, double y, double z) {
        public static final Codec<Position> CODEC = create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("x").forGetter(Position::x),
                Codec.DOUBLE.fieldOf("y").forGetter(Position::y),
                Codec.DOUBLE.fieldOf("z").forGetter(Position::z)).apply(instance, Position::new));
    }
}