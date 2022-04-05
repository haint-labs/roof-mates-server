package labs.haint.database

import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.spi.Row
import kotlinx.coroutines.reactive.awaitSingle

interface DatabaseContext {
    val factory: PostgresqlConnectionFactory
}

suspend inline fun <reified T> DatabaseContext.one(
    query: String,
    params: List<Any> = emptyList(),
    crossinline mapper: (row: Row) -> T
): T {
    return factory.create()
        .flatMap { connection ->
            connection.createStatement(query)
                .apply { params.forEachIndexed { index, it -> bind(index, it) } }
                .execute()
                .flatMap { it.map { row, _ -> mapper(row) } }
                .single()
        }
        .awaitSingle()
}

suspend inline fun <reified T> DatabaseContext.list(
    query: String,
    params: List<Any> = emptyList(),
    crossinline mapper: (row: Row) -> T
): List<T> {
    return factory.create()
        .flatMapMany { connection ->
            connection.createStatement(query)
                .apply { params.forEachIndexed { index, it -> bind(index, it) } }
                .execute()
                .flatMap { it.map { row, _ -> mapper(row) } }
                .collectList()
        }
        .awaitSingle()
}
