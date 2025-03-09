package org.alpacaindustries.config

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties

object ConfigLoader {

  private const val CONFIG_FILE = "config.properties"
  private val CONFIG_PATH: Path = Paths.get("./", CONFIG_FILE)

  @Throws(IOException::class)
  fun loadConfig(): Config {
    val properties = Properties()

    // Check if the config file exists, if not create it with default values
    if (Files.notExists(CONFIG_PATH)) {
      createDefaultConfigFile()
    }

    // Load the properties from the config file
    Files.newInputStream(CONFIG_PATH).use { input -> properties.load(input) }

    val host = properties.getProperty("server.host") ?: "0.0.0.0"
    val port = properties.getProperty("server.port")?.toInt() ?: 25565
    val velocityEnabled = properties.getProperty("velocity.enabled")?.toBoolean() ?: false
    val velocitySigned = properties.getProperty("velocity.signed")?.toBoolean() ?: false
    val velocityForwardingSecret = properties.getProperty("velocity.forwardingSecret") ?: ""

    return Config(host, port, velocityEnabled, velocitySigned, velocityForwardingSecret)
  }

  @Throws(IOException::class)
  private fun createDefaultConfigFile() {
    val defaultProperties =
            Properties().apply {
              setProperty("server.host", "0.0.0.0")
              setProperty("server.port", "25565")
              setProperty("velocity.enabled", "false")
              setProperty("velocity.signed", "false")
              setProperty("velocity.forwardingSecret", "")
            }

    // Create the directories if they don't exist
    Files.createDirectories(CONFIG_PATH.parent)

    // Write the default properties to the config file
    Files.newOutputStream(CONFIG_PATH).use { output ->
      defaultProperties.store(output, "Default configuration")
    }
  }
}
