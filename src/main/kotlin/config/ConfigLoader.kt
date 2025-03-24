package org.alpacaindustries.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object ConfigLoader {

        private const val CONFIG_FILE = "config.json"
        private val CONFIG_PATH: Path = Paths.get("./", CONFIG_FILE)
        private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

        @Throws(IOException::class)
        fun loadConfig(): Config {
                // Check if the config file exists, if not create it with default values
                if (Files.notExists(CONFIG_PATH)) {
                        createDefaultConfigFile()
                }

                // Load the config from JSON file
                return Files.newBufferedReader(CONFIG_PATH).use { reader ->
                        gson.fromJson(reader, Config::class.java)
                }
        }

        @Throws(IOException::class)
        private fun createDefaultConfigFile() {
                val defaultConfig =
                        Config(
                                host = "0.0.0.0",
                                port = 25565,
                                svcPort = 25565,
                                velocityEnabled = false,
                                velocitySigned = false,
                                velocityForwardingSecret = "",
                                npcs =
                                        listOf(
                                                NPC(
                                                        name = "Example NPC",
                                                        server = "lobby",
                                                        skinValue =
                                                                "eyJ0aW1lc3RhbXAiOjE1NzM3NTc0NDgxMjQsInByb2ZpbGVJZCI6IjdkNjU1MjQ5NGZiZDQyODdiNjgyMjJkMTdjNjVhM2ZkIiwicHJvZmlsZU5hbWUiOiJCdWRkaGEiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE3YzZjNDU4NWJjMmRjZWM2ODU1N2YwZDVmMmE1NTNkYzEyY2RjY2JmNjBiOGI3OWM4NDI0NWMxMzVmZDcifX19",
                                                        skinSignature =
                                                                "AF19kyc6XRBQGUr6JYZQpRiUngfHRa/oRh/t+xORrWcBSWrl1EzZ5QMxZeZLeE+e4r2dYRvlH/0U98Ccgi5zY05n4I9Na6jo3V7gpQZ2X8eDtq9wQ4ZWKh2kCg6HJgGWkvRncGAmLDZvxMw0QgJC2nxqZl+nV5jkruA5i0HY+lpGQ2q7yA9tmmNgdvIuSY7yFsh2wHm7thnSth+Y8RqRgKu/kAZESpQ4/Vbl5/YukHdiJ3nWHrOfW489zN4HlEqJ1KKVePdlpLYAvNslZdt7oN+BEq+aEzMZd4WL+kLzVE4ILarzTiJPIcibFHXS8+bc3OSFVdbrmvNBbIkRwJDrt6G0QWeT4KwJwwTfkrwZVoRo/tOm1nEL6oiKWUDZrf0GCLEXkKubI8QrqJ7KEU6vB73wVUwXzGBFryC3XBoPVnGQHLnqKuQSSB/ChESrLiPSsVLRj+SgLK8lQrCnm+nBZOGiK+w6/qq5xWpu0hpYlbK1MUteyv7Yyl/jpApf7s2HxuLZgN7s1gMr8nKtzis4U0BBbO3NJ95wlGSS2qJBHJYImtd0rUQqLTaG2BU0k0nNOzGVuZo/sV5pKrvoqx35xXAm0/WK+AgRtgJCVka4Czl/JIRhvpJi9I+5ijDul1VxEVTMsOcBd15tyi6dITTUuRNVGCsRVT7Bhq9qP8AvU0k=",
                                                        x = 0.0,
                                                        y = 42.0,
                                                        z = 0.0,
                                                        yaw = 0f,
                                                        pitch = 0f
                                                )
                                        ),
                                ops = emptyList()
                        )

                // Create the directories if they don't exist
                Files.createDirectories(CONFIG_PATH.parent)

                // Write the default config to the JSON file
                Files.newBufferedWriter(CONFIG_PATH).use { writer ->
                        gson.toJson(defaultConfig, writer)
                }
        }
}
