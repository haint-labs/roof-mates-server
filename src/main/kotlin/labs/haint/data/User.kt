package labs.haint.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface User {
    val name: String
    val surname: String
    val phoneNumber: String
    val device: Device
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
) : User

@Serializable
@SerialName("guest")
data class Guest(
    override val name: String,
    override val surname: String,
    override val phoneNumber: String,
    override val device: Device,
) : User