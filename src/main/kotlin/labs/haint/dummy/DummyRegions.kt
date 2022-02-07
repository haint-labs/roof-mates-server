package labs.haint.dummy

import labs.haint.data.Region
import labs.haint.data.Spot

val dummyRegions = mutableListOf(
    Region(id = 1, address = "Road street 16"),
    Region(id = 2, address = "Road street 17"),
    Region(id = 3, address = "Road street 19"),
    Region(id = 4, address = "Road street 20"),
)

val dummySpots = mutableListOf(
    Spot(id = 1, regionId = 1, number = 1u),
    Spot(id = 2, regionId = 1, number = 2u),
    Spot(id = 3, regionId = 2, number = 3u),
    Spot(id = 4, regionId = 2, number = 4u),
    Spot(id = 5, regionId = 3, number = 30u),
    Spot(id = 5, regionId = 4, number = 55u),
)