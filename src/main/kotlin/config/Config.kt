package org.alpacaindustries.config

data class Config(
        val host: String,
        val port: Int,
        val velocityEnabled: Boolean,
        val velocitySigned: Boolean,
        val velocityForwardingSecret: String
)
