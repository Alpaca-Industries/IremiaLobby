package me.greysilly7.npcsmadeasy.config;

public class NPC {
    private final String name;
    private final Position position;
    private final String serverToFowardPlayerTo;
    private final float yaw;
    private final float pitch;

    // Constructor
    public NPC(String name, Position position, String serverToFowardPlayerTo, float yaw, float pitch) {
        this.name = name;
        this.position = position;
        this.serverToFowardPlayerTo = serverToFowardPlayerTo;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    // Getters
    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }

    public String getServerToFowardPlayerTo() {
        return serverToFowardPlayerTo; // Getter for the command
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    // Inner class for Position
    public static class Position {
        private double x;
        private double y;
        private double z;

        // Constructor
        public Position(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        // Getters
        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }
    }
}
