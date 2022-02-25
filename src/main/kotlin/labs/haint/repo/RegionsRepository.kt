package labs.haint.repo

import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.api.PostgresqlResult
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.json.Json
import labs.haint.data.Region
import labs.haint.data.Spot

interface RegionsRepository {
    suspend fun all(): List<Region>
    suspend fun save(region: Region)
    suspend fun save(spot: Spot)
}

class PostgresRegionsRepository(
    private val factory: PostgresqlConnectionFactory,
    private val json: Json,
) : RegionsRepository {
    override suspend fun all(): List<Region> {
        val sql = """
            select
                json_build_object(
                    'id', regions.id,
                    'address', regions.address,
                    'spots', coalesce(
						json_agg(
							json_build_object(
								'id', spots.id, 
								'regionId', regions.id,
								'number', spots.parking_number
							)
                    	) filter (where spots.id is not null),
						'[]'
                	)
				) as result
            from regions 
            left join spots on regions.id = spots.region_id
            group by regions.id
        """.trimIndent()

        val regions = factory.create()
            .flatMapMany { connection ->
                connection.createStatement(sql)
                    .execute()
                    .flatMap {
                        it.map { row, _ ->
                            json.decodeFromString(Region.serializer(), row.get("result", String::class.java)!!)
                        }
                    }
            }
            .collectList()
            .awaitSingle()

        return regions
    }

    override suspend fun save(region: Region) {
        factory.create()
            .flatMap {
                it.createStatement("insert into regions(id, address) values (default, $1)")
                    .bind(0, region.address)
                    .execute()
                    .flatMap(PostgresqlResult::getRowsUpdated)
                    .single()
            }
            .awaitSingle()
    }

    override suspend fun save(spot: Spot) {
        factory.create()
            .flatMap {
                it.createStatement("insert into spots (id, region_id, parking_number) values (default, $1, $2)")
                    .bind(0, spot.regionId)
                    .bind(1, spot.number)
                    .execute()
                    .flatMap(PostgresqlResult::getRowsUpdated)
                    .single()
            }
            .awaitSingle()
    }
}