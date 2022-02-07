package labs.haint

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import labs.haint.data.Guest
import labs.haint.data.Owner
import labs.haint.data.User
import labs.haint.repo.InMemoryRegionsRepository
import labs.haint.repo.InMemoryUserRepository
import labs.haint.routes.regions
import labs.haint.routes.users

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.main() {
    val json = Json {
        prettyPrint = true
        serializersModule = SerializersModule {
            polymorphic(User::class) {
                subclass(Guest::class, Guest.serializer())
                subclass(Owner::class, Owner.serializer())
            }
        }
    }

    val regionsRepo = InMemoryRegionsRepository()
    val usersRepo = InMemoryUserRepository(regionsRepo)

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