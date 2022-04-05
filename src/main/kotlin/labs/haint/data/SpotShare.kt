package labs.haint.data

import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.api.PostgresqlResult
import io.r2dbc.spi.Row
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import labs.haint.database.DatabaseContext
import labs.haint.database.list
import kotlin.reflect.KProperty

@Serializable
data class SpotShare(
    val spot_id: Long,
    val user_id: Long,
    val from_timestamp: Int,
    val to_timestamp: Int,
    val id: Long = -1,
)

@Serializable
data class SpotRequest(
    val user_id: Long,
    val from_timestamp: Int,
    val to_timestamp: Int,
    val regions: List<Long> = emptyList(),
    val id: Long = -1,
)

@Serializable
data class SpotBooking(
    val user_id: Long,
    val spot_share_id: Long,
    val from_timestamp: Int,
    val to_timestamp: Int,
    val id: Long = -1,
)

interface SpotsRepository {
    // spot dao
    suspend fun save(spot: Spot)
    suspend fun spots(): List<Spot>

    // spot share dao
    suspend fun save(share: SpotShare)
    suspend fun delete(share: SpotShare)
    suspend fun findBy(regions: List<Long>, from: Int, to: Int): List<SpotShare>

    // spot booking dao
    suspend fun save(booking: SpotBooking)
    suspend fun delete(booking: SpotBooking)

    // spot request dao
    suspend fun save(request: SpotRequest)
    suspend fun deleteRequestFor(userId: Long)
    suspend fun pendingRequests(regionId: Long, from: Int, to: Int): List<SpotRequest>

    suspend fun regionIdBy(spotId: Long): Long
}


inline operator fun <reified T> Row.invoke(key: String): T =
    this.get(key, T::class.java) ?: throw Error("Unable to get $key from database row")

inline operator fun <reified T> Row.invoke(prop: KProperty<T>): T =
    this.get(prop.name, T::class.java) ?: throw Error("Unable to get ${prop.name} from database row")

class PostgresSpotsRepository(
    override val factory: PostgresqlConnectionFactory,
    private val json: Json,
) : DatabaseContext, SpotsRepository {
    override suspend fun save(spot: Spot) {
        TODO("Not yet implemented")
    }

    override suspend fun spots(): List<Spot> {
        return list("select row_to_json(*) from spots") {
            Spot(id = it("id"), number = it("number"), regionId = it("region_id"))
        }
    }

    override suspend fun save(share: SpotShare) {
        val sql = """
            insert into spot_shares (id, spot_id, user_id, from_timestamp, to_timestamp) values 
                (default, $1, $2, $3, $4)
        """.trimIndent()

        factory.create()
            .flatMap {
                it.createStatement(sql)
                    .bind(0, share.spot_id)
                    .bind(1, share.user_id)
                    .bind(2, share.from_timestamp)
                    .bind(3, share.to_timestamp)
                    .execute()
                    .flatMap(PostgresqlResult::getRowsUpdated)
                    .single()
            }
            .awaitSingle()
    }

    override suspend fun delete(share: SpotShare) {
        val sql = """
            delete from spot_shares where id = $1
        """.trimIndent()

        factory.create()
            .flatMap {
                it.createStatement(sql)
                    .bind(0, share.spot_id)
                    .execute()
                    .flatMap(PostgresqlResult::getRowsUpdated)
                    .single()
            }
            .awaitSingle()
    }

    override suspend fun findBy(regions: List<Long>, from: Int, to: Int): List<SpotShare> {
        TODO("Not yet implemented")
    }

    override suspend fun save(booking: SpotBooking) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(booking: SpotBooking) {
        TODO("Not yet implemented")
    }

    override suspend fun save(request: SpotRequest) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteRequestFor(userId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun pendingRequests(regionId: Long, from: Int, to: Int): List<SpotRequest> {
        TODO()
    }

    override suspend fun regionIdBy(spotId: Long): Long {
        val sql = """
            select region_id from spots where id = $1 limit 1
        """.trimIndent()

        return factory.create()
            .flatMap { connection ->
                connection.createStatement(sql)
                    .bind(0, spotId)
                    .execute()
                    .flatMap {
                        it.map { row, _ -> row.get(0, Integer::class.java)!! }
                    }
                    .single()
            }
            .awaitSingle()
            .toLong()
    }
}