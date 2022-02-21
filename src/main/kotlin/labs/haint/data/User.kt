package labs.haint.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface User {
    val id: Long
    val name: String
    val surname: String
    val phoneNumber: String
    val device: Device
}

val User.type get() = when (this) {
    is Owner -> "owner"
    is Guest -> "guest"
}

@Serializable
@SerialName("owner")
data class Owner(
    override val name: String,
    override val surname: String,
    override val phoneNumber: String,
    override val device: Device,
    val regionId: Long,
    val parkingNumber: UInt,
    override val id: Long = -1
) : User

@Serializable
@SerialName("guest")
data class Guest(
    override val name: String,
    override val surname: String,
    override val phoneNumber: String,
    override val device: Device,
    override val id: Long = -1
) : User