package labs.haint.data

import kotlinx.serialization.Serializable

enum class DeviceType {
    iOS, Android
}

@Serializable
data class Device(
    val type: DeviceType,
    val token: String
)