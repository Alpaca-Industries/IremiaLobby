package org.alpacaindustries.lobbyServer.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigLoader {

  private static final String CONFIG_FILE = "config.properties";
  private static final Path CONFIG_PATH = Paths.get("./", CONFIG_FILE);

  public static Config loadConfig() throws IOException {
    Properties properties = new Properties();

    // Check if the config file exists, if not create it with default values
    if (!Files.exists(CONFIG_PATH)) {
      createDefaultConfigFile();
    }

    // Load the properties from the config file
    try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
      properties.load(input);
    }

    String host = properties.getProperty("server.host");
    int port = Integer.parseInt(properties.getProperty("server.port"));
    boolean velocityEnabled = Boolean.parseBoolean(properties.getProperty("velocity.enabled"));
    boolean velocitySigned = Boolean.parseBoolean(properties.getProperty("velocity.signed"));
    String velocityForwardingSecret = properties.getProperty("velocity.forwardingSecret");

    return new Config(host, port, velocityEnabled, velocitySigned, velocityForwardingSecret);
  }

  private static void createDefaultConfigFile() throws IOException {
    Properties defaultProperties = new Properties();
    defaultProperties.setProperty("server.host", "0.0.0.0");
    defaultProperties.setProperty("server.port", "25565");
    defaultProperties.setProperty("velocity.enabled", "false");
    defaultProperties.setProperty("velocity.signed", "false");
    defaultProperties.setProperty("velocity.forwardingSecret", "");

    // Create the directories if they don't exist
    Files.createDirectories(CONFIG_PATH.getParent());

    // Write the default properties to the config file
    try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
      defaultProperties.store(output, "Default configuration");
    }
  }
}