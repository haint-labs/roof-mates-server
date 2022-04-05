package labs.haint.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import labs.haint.data.SpotBooking
import labs.haint.data.SpotsRepository
import labs.haint.data.SpotRequest
import labs.haint.data.SpotShare

fun Application.spots(
    repo: SpotsRepository,
) = routing {
    route("/spots") {
        get {
            // TODO: ktor-locations
            val regions = call.parameters.getAll("regions")
            val from = call.parameters["from"]?.toIntOrNull()
            val to = call.parameters["to"]?.toIntOrNull()
        }

        post("/share") {
            val share = call.receive<SpotShare>()
            repo.save(share)

            // convert spot to region
            // find all matching requests
            // send notification

            call.respond(HttpStatusCode.Created)
        }

        post("/book") {
            val booking = call.receive<SpotBooking>()

            repo.save(booking)
            repo.deleteRequestFor(booking.user_id)

            call.respond(HttpStatusCode.Created)
        }

        post("/request") {
            val request = call.receive<SpotRequest>()
            repo.save(request)

            val available = repo.findBy(request.regions, request.from_timestamp, request.to_timestamp)
            if (available.isEmpty()) {
                // convert spot to region
                // filter out busy spots
                // send notification

            } else {
                // send notification with list of spots
            }

            call.respond(HttpStatusCode.Created)
        }
    }
}