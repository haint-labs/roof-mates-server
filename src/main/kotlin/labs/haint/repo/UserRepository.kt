package labs.haint.repo

import labs.haint.data.Owner
import labs.haint.data.User
import kotlin.Error

interface UserRepository {
    suspend fun all(): List<User>
    suspend fun save(user: User)
}

class InMemoryRepository(
    private val users: MutableList<User> = mutableListOf(),
) : UserRepository {
    override suspend fun all(): List<User> = users

    override suspend fun save(user: User) {
        users.find { it.phoneNumber == user.phoneNumber }?.let {
            throw Error("User with same phone number exists")
        }

        if (user is Owner) {
            users.find { it is Owner && it.parkingNumber == user.parkingNumber }?.let {
                throw Error("User with same parking number already exists")
            }
        }

        users += user
    }
}