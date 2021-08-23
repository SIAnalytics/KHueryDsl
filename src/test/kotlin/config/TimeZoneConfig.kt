package config

import java.util.*

data class TimeZoneConfig(
    private val timeZoneId: String
) : Config {
    val timeZone: TimeZone = TimeZone.getTimeZone(timeZoneId)
}
