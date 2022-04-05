package labs.haint

import io.ktor.server.application.*
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import labs.haint.data.*
import labs.haint.repo.PostgresRegionsRepository
import labs.haint.repo.PostgresUserRepository
import labs.haint.repo.RegionsRepository
import labs.haint.repo.UsersRepository

interface AppContext {
    val environment: Env
    val serializer: Json
    val repositories: Repositories
}

interface Repositories {
    val regions: RegionsRepository
    val users: UsersRepository
    val spots: SpotsRepository
}

fun createAppContext(environment: ApplicationEnvironment): AppContext {
    val json = createSerializer()

    val connectionFactory = createConnectionFactory()

    val repositories = createRepositories(connectionFactory, json)

    val env = when (environment.config.property("ktor.environment").getString()) {
        "development" -> Env.Development
        "production" -> Env.Production
        else -> throw IllegalStateException("Unknown environment")
    }

    return object : AppContext {
        override val environment get() = env
        override val serializer get() = json
        override val repositories get() = repositories
    }
}

fun createSerializer() = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    serializersModule = SerializersModule {
        polymorphic(User::class) {
            subclass(Guest::class, Guest.serializer())
            subclass(Owner::class, Owner.serializer())
        }
    }
}

fun createConnectionFactory(): PostgresqlConnectionFactory {
    val config = PostgresqlConnectionConfiguration.builder()
        .host(System.getenv("POSTGRES_HOST"))
        .database(System.getenv("POSTGRES_DB"))
        .username(System.getenv("POSTGRES_USER"))
        .password(System.getenv("POSTGRES_PASSWORD"))
        .build()

    return PostgresqlConnectionFactory(config)
}

fun createRepositories(
    connectionFactory: PostgresqlConnectionFactory,
    json: Json
): Repositories {
    val regions = PostgresRegionsRepository(connectionFactory, json)
    val users = PostgresUserRepository(connectionFactory, json)
    val spots = PostgresSpotsRepository(connectionFactory, json)

    return object : Repositories {
        override val regions get() = regions
        override val users get() = users
        override val spots get() = spots
    }
}