package me.greysilly7.npcsmadeasy.config;

import com.mojang.serialization.Codec;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;

public record MineSkin(boolean enable, String authKey) {
  public static final Codec<MineSkin> CODEC = create(instance -> instance.group(
      Codec.BOOL.fieldOf("enable").forGetter(MineSkin::enable),
      Codec.STRING.fieldOf("authKey").forGetter(MineSkin::authKey)).apply(instance, MineSkin::new));
}