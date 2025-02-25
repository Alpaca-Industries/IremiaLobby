package me.greysilly7.npcsmadeasy.util.mineskin;

public record MineSkinResponse(
    String uuid,
    String name,
    String visibility,
    String variant,
    Texture texture,
    Hash hash,
    Url url,
    Generator generator,
    int views,
    boolean duplicate) {
  public record Texture(
      Data data) {
    public record Data(
        String value,
        String signature) {
    }
  }

  public record Hash(
      String skin,
      String cape) {
  }

  public record Url(
      String skin,
      String cape) {
  }

  public record Generator(
      String version,
      long timestamp,
      long duration,
      String account,
      String server) {
  }
}