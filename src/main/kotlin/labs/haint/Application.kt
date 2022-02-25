package labs.haint

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import labs.haint.data.Guest
import labs.haint.data.Owner
import labs.haint.data.User
import labs.haint.repo.PostgresRegionsRepository
import labs.haint.repo.PostgresUserRepository
import labs.haint.routes.regions
import labs.haint.routes.users

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.main() {
    val json = json()

    val connectionFactory = databaseConnectionFactory()

    val regionsRepo = PostgresRegionsRepository(connectionFactory, json)
    val usersRepo = PostgresUserRepository(connectionFactory, json)

    install(ContentNegotiation) { json(json) }

    handleExceptions()

    users(repo = usersRepo)
    regions(repo = regionsRepo)
}

fun Application.handleExceptions() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            cause.printStackTrace()

            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}

fun json() = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    serializersModule = SerializersModule {
        polymorphic(User::class) {
            subclass(Guest::class, Guest.serializer())
            subclass(Owner::class, Owner.serializer())
        }
    }
}

fun databaseConnectionFactory(): PostgresqlConnectionFactory {
    val config = PostgresqlConnectionConfiguration.builder()
        .host(System.getenv("POSTGRES_HOST"))
        .database(System.getenv("POSTGRES_DB"))
        .username(System.getenv("POSTGRES_USER"))
        .password(System.getenv("POSTGRES_PASSWORD"))
        .build()

    return PostgresqlConnectionFactory(config)
}