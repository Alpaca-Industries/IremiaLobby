package me.greysilly7.npcsmadeasy.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.google.gson.Gson;

import me.greysilly7.npcsmadeasy.NPCMadeEasyMod;
import me.greysilly7.npcsmadeasy.config.MineSkin;
import me.greysilly7.npcsmadeasy.util.mineskin.ErrorResponse;
import me.greysilly7.npcsmadeasy.util.mineskin.MineSkinResponse;

public class MojangSkinGenerator {

  private static final Gson GSON = new Gson();

  public static Object fetchFromUUID(final String uuid) {
    MineSkin mineSkin = NPCMadeEasyMod.CONFIG.getMineSkin();

    String urlString = "https://api.mineskin.org/v2/skins/" + uuid;
    HttpURLConnection connection = null;

    try {
      URL url = new URI(urlString).toURL();
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Accept", "application/json");
      connection.setRequestProperty("Authorization", "Bearer " + mineSkin.authKey());

      int responseCode = connection.getResponseCode();
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      StringBuilder response = new StringBuilder();
      String inputLine;

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      if (responseCode == HttpURLConnection.HTTP_OK) {
        // Parse the response to a MineSkinResponse object
        return GSON.fromJson(response.toString(), MineSkinResponse.class);
      } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
        // Parse the response to an ErrorResponse object
        return GSON.fromJson(response.toString(), ErrorResponse.class);
      } else {
        NPCMadeEasyMod.LOGGER.error("GET request failed with response code: {}", responseCode);
      }
    } catch (IOException | URISyntaxException e) {
      NPCMadeEasyMod.LOGGER.error("Exception occurred while fetching skin from UUID: {}", e.getMessage());
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
    return null;
  }
}