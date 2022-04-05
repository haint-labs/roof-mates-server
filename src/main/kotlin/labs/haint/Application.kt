package labs.haint

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import kotlinx.coroutines.launch
import labs.haint.dev.developmentData
import labs.haint.routes.regions
import labs.haint.routes.spots
import labs.haint.routes.users

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.main() {
    val context = createAppContext(environment)

    install(ContentNegotiation) { json(context.serializer) }

    handleExceptions()

    users(repo = context.repositories.users)
    regions(repo = context.repositories.regions)
    spots(repo = context.repositories.spots)

    launch {
        if (context.environment == Env.Production) return@launch

        developmentData(context)
    }
}

fun Application.handleExceptions() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            cause.printStackTrace()

            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}