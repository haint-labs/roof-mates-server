package labs.haint.repo

import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.api.PostgresqlConnection
import io.r2dbc.postgresql.api.PostgresqlResult
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.json.Json
import labs.haint.data.*
import reactor.core.publisher.Mono
import java.util.*

interface UsersRepository {
    suspend fun all(): List<User>
    suspend fun save(user: User)
}

class PostgresUserRepository(
    private val factory: PostgresqlConnectionFactory,
    private val json: Json,
) : UsersRepository {
    override suspend fun all(): List<User> {
        val sql = """
            select 
                type::varchar,
                json_build_object(
                    'id', users.id,
                    'name', users.name,
                    'surname', users.surname,
                    'phoneNumber', users.phone_number,
                    'device', json_build_object(
                        'type', case 
                            when devices.device_os = 'android' then 'Android'
                            when devices.device_os = 'ios' then 'iOS'
                        end,
                        'token', devices.token
                    ),
                    'regionId', spots.region_id,
                    'parkingNumber', spots.parking_number
                ) as result
            from users
            left join devices on users.device_id = devices.id
            left join spots on users.spot_id = spots.id
        """.trimIndent()

        val users = factory.create()
            .flatMapMany { connection ->
                connection.createStatement(sql)
                    .execute()
                    .flatMap {
                        it.map { row, _ ->
                            val serializer = when (val type = row.get("type", String::class.java)!!) {
                                "owner" -> Owner.serializer()
                                "guest" -> Guest.serializer()
                                else -> throw Error("Unable to deserialize user - type $type")
                            }

                            json.decodeFromString(serializer, row.get("result", String::class.java)!!)
                        }
                    }
                    .collectList()
            }
            .awaitSingle()

        return users
    }

    override suspend fun save(user: User) {
        val insertDevice = { connection: PostgresqlConnection, device: Device ->
            val sql = """
                insert into devices (id, device_os, token) values 
                    (default, $1::device_os, $2)
            """.trimIndent()

            connection.createStatement(sql)
                .returnGeneratedValues()
                .bind(0, device.type.toString().lowercase())
                .bind(1, device.token)
                .execute()
                .flatMap {
                    it.map { row, _ -> row.get(0, Integer::class.java) }
                }
                .single()
        }

        val insertSpot = { connection: PostgresqlConnection, spot: Spot ->
            val sql = """
                insert into spots (id, region_id, parking_number) values 
                    (default, $1, $2)
             """.trimIndent()

            connection.createStatement(sql)
                .returnGeneratedValues()
                .bind(0, spot.regionId)
                .bind(1, spot.number.toInt())
                .execute()
                .flatMap {
                    it.map { row, _ -> row.get(0, Integer::class.java) }
                }
                .map { Optional.of(it) }
                .single()
        }

        val insertUser = { connection: PostgresqlConnection, user: User, deviceId: Long?, spotId: Long? ->
            val sql = """
                insert into users (id, type, name, surname, phone_number, device_id, spot_id) values 
                    (default, $1::user_type, $2, $3, $4, $5, $6)
            """.trimIndent()

            connection.createStatement(sql)
                .apply {
                    bind(0, user.type)
                    bind(1, user.name)
                    bind(2, user.surname)
                    bind(3, user.phoneNumber)
                    deviceId?.let { bind(4, it) } ?: bindNull(4, Integer::class.java)
                    spotId?.let { bind(5, it) } ?: bindNull(5, Integer::class.java)
                }
                .execute()
                .flatMap(PostgresqlResult::getRowsUpdated)
                .single()
        }

        val insertUserTransaction = { connection: PostgresqlConnection ->
            val insertSpotIfOwner = when (user) {
                is Owner -> insertSpot(
                    connection,
                    Spot(number = user.parkingNumber, regionId = user.regionId)
                )
                else -> Mono.just(Optional.empty<Int>())
            }

            connection.beginTransaction()
                .then(
                    Mono.zip(
                        insertDevice(connection, user.device),
                        insertSpotIfOwner,
                    )
                )
                .flatMap { tuple ->
                    val deviceId = tuple.t1.toLong()
                    val spotId = tuple.t2
                        .takeUnless { it.isEmpty }
                        ?.map { it.toLong() }
                        ?.get()

                    insertUser(connection, user, deviceId, spotId)
                }
                .then()
        }

        Mono
            .usingWhen(
                factory.create(),
                insertUserTransaction,
                PostgresqlConnection::commitTransaction,
                { connection, _ -> connection.rollbackTransaction() },
                PostgresqlConnection::rollbackTransaction,
            )
            .awaitFirstOrNull()
    }
}