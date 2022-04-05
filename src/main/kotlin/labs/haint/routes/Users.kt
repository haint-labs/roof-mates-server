package labs.haint.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import labs.haint.data.User
import labs.haint.repo.UsersRepository

fun Application.users(
    repo: UsersRepository
) = routing {
    route("/users") {
        get {
            val users = repo.all()
            call.respond(HttpStatusCode.OK, users)
        }

        post("/register") {
            val user = call.receive<User>()

            runCatching { repo.save(user) }
                .onSuccess { call.respond(HttpStatusCode.Created) }
                .onFailure {
                    call.respondText(
                        status = HttpStatusCode.BadRequest,
                        text = it.message ?: ""
                    )
                }
        }
    }
}