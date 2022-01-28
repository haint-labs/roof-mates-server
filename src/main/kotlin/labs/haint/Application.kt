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
import labs.haint.repo.InMemoryRepository
import labs.haint.routes.users

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.main() {
    val json = Json {
        serializersModule = SerializersModule {
            polymorphic(User::class) {
                subclass(Guest::class, Guest.serializer())
                subclass(Owner::class, Owner.serializer())
            }
        }
    }

    val usersRepo = InMemoryRepository()

    install(ContentNegotiation) { json(json) }
    handleExceptions()

    users(repo = usersRepo)
}

fun Application.handleExceptions() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            cause.printStackTrace()

            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}