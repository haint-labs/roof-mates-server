package labs.haint.repo

import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.api.PostgresqlResult
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.json.Json
import labs.haint.data.Region
import labs.haint.data.Spot
import reactor.core.publisher.Mono

interface RegionsRepository {
    suspend fun all(): List<Region>
    suspend fun save(region: Region)
    suspend fun save(spot: Spot)
    suspend fun byId(id: Long): Region?
}

class InMemoryRegionsRepository(
    private val factory: PostgresqlConnectionFactory,
) : RegionsRepository {
    override suspend fun all(): List<Region> {
        val sql = """
            select
                json_build_object(
                    'id', regions.id,
                    'address', regions.address,
                    'spots', json_agg(
                        json_build_object(
                            'id', spots.id, 
                            'regionId', regions.id,
                            'number', spots.parking_number
                        )
                    )
                ) as result
            from regions 
            join spots on regions.id = spots.region_id
            group by regions.id
        """.trimIndent()

        val regions = factory.create()
            .flatMapMany { connection ->
                connection.createStatement(sql)
                    .execute()
                    .flatMap {
                        it.map { row ->
                            Json.decodeFromString(Region.serializer(), row.get("result", String::class.java)!!)
                        }
                    }
            }
            .collectList()
            .awaitSingle()

        return regions
    }

    override suspend fun save(region: Region) {
        val sharedConnection = factory.create().share()

        sharedConnection
            .flatMapMany { connection ->
                connection.createStatement("select count(*) from regions where address = $1")
                    .bind("$1", region.address)
                    .execute()
                    .flatMap { it.map { row -> row.get(0, Integer::class.java) } }
                    .map { it > 0 }
            }
            .flatMap {
                if (it == true) {
                    return@flatMap Mono.error(
                        Error("Region with same address already exists")
                    )
                }

                sharedConnection
            }
            .flatMap {
                it.createStatement("insert into regions(id, address) values (default, $1)")
                    .bind(0, region.address)
                    .execute()
                    .flatMap(PostgresqlResult::getRowsUpdated)
            }
            .awaitSingle()
    }

    override suspend fun save(spot: Spot) {
        val sharedConnection = factory.create().share()

        sharedConnection
            .flatMapMany { connection ->
                connection.createStatement("select count(*) from spots where number = $1")
                    .bind("$1", spot.number)
                    .execute()
                    .flatMap { it.map { row -> row.get(0, Integer::class.java) } }
                    .map { it > 0 }
            }
            .flatMap {
                if (it == true) {
                    return@flatMap Mono.error(
                        Error("Spot with same number already exists")
                    )
                }

                sharedConnection
            }
            .flatMap {
                it.createStatement("insert into spots (id, region_id, parking_number) values (default, $1, $2)")
                    .bind(0, spot.regionId)
                    .bind(1, spot.number)
                    .execute()
                    .flatMap(PostgresqlResult::getRowsUpdated)
            }
            .awaitSingle()
    }

    override suspend fun byId(id: Long): Region? {
        val sql = """
            select
                json_build_object(
                    'id', regions.id,
                    'address', regions.address,
                    'spots', json_agg(
                        json_build_object(
                            'id', spots.id, 
                            'regionId', regions.id,
                            'number', spots.parking_number
                        )
                    )
                ) as result
            from regions 
            join spots on regions.id = spots.region_id
            group by regions.id
            where regions.id = $1
        """.trimIndent()

        val regions = factory.create()
            .flatMapMany { connection ->
                connection.createStatement(sql)
                    .bind(0, id)
                    .execute()
                    .flatMap {
                        it.map { row ->
                            Json.decodeFromString(Region.serializer(), row.get("result", String::class.java)!!)
                        }
                    }
            }
            .collectList()
            .awaitSingle()

        return regions.first()
    }
}