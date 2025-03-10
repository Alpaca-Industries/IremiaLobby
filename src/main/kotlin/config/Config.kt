package org.alpacaindustries.config

data class Config(
        val host: String,
        val port: Int,
        val velocityEnabled: Boolean,
        val velocitySigned: Boolean,
        val velocityForwardingSecret: String,
        val npcs: List<NPC> = emptyList()
)

data class NPC(
        val name: String,
        val server: String,
        val skinValue: String,
        val skinSignature: String,
        val x: Double,
        val y: Double,
        val z: Double,
        val yaw: Float,
        val pitch: Float
)
