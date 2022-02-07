package labs.haint.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import labs.haint.data.Region
import labs.haint.repo.RegionsRepository

fun Application.regions(
    repo: RegionsRepository
) = routing {
    route("/regions") {
        get {
            val regions = repo.all()
            call.respond(HttpStatusCode.OK, regions)
        }

        post {
            val region = call.receive<Region>()

            runCatching { repo.save(region) }
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