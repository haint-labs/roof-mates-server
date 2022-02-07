package labs.haint.repo

import labs.haint.data.Owner
import labs.haint.data.Spot
import labs.haint.data.User

interface UserRepository {
    suspend fun all(): List<User>
    suspend fun save(user: User)
}

class InMemoryUserRepository(
    private val regions: RegionsRepository,
    private val users: MutableList<User> = mutableListOf(),
) : UserRepository {
    override suspend fun all(): List<User> = users

    override suspend fun save(user: User) {
        users.find { it.phoneNumber == user.phoneNumber }?.let {
            throw Error("User with same phone number exists")
        }

        if (user is Owner) {
            requireNotNull(regions.byId(user.regionId)) { "Invalid region id" }

            regions.save(
                Spot(
                    number = user.parkingNumber,
                    regionId = user.regionId
                )
            )
        }

        users += user
    }
}