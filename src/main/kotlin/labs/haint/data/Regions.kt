package labs.haint.data

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
data class Region(
    val address: String,
    @EncodeDefault val spots: List<Spot> = emptyList(),
    val id: Long = -1,
)

@Serializable
data class Spot(
    val number: UInt,
    val regionId: Long,
    val id: Long = -1,
)
