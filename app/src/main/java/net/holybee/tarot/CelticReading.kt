package net.holybee.tarot

import java.io.Serializable


data class CelticReading(
    var card: Card,
    var result: String,
    var done: Boolean
) : Serializable
