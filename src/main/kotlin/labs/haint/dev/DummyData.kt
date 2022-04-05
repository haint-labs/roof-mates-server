package labs.haint.dev

import labs.haint.AppContext
import labs.haint.data.*

suspend fun developmentData(app: AppContext) {
    val dummyRegions = listOf(
        Region("Road street 16", id = 1),
        Region("Road street 17"),
        Region("Road street 19"),
        Region("Road street 20"),
    )

    runCatching {
        dummyRegions.forEach { app.repositories.regions.save(it) }
    }

    val dummyUsers = listOf(
        Guest(
            name = "Johny",
            surname = "Boy",
            phoneNumber = "+37121231233",
            device = Device(
                type = DeviceType.iOS,
                token = "asdfnkdn5645ASGgw"
            ),
        ),
        Owner(
            name = "Sara",
            surname = "Connor",
            phoneNumber = "+3714765757",
            device = Device(
                type = DeviceType.Android,
                token = "asdfnkdn56sdfgssdfg",
            ),
            regionId = 1,
            parkingNumber = 45u,
        )
    )

    runCatching {
        dummyUsers.forEach { app.repositories.users.save(it) }
    }
}