package org.alpacaindustries.lobbyServer.config;

public record Config(String host, int port, boolean velocityEnabled, boolean velocitySigned,
    String velocityForwardingSecret) {
}