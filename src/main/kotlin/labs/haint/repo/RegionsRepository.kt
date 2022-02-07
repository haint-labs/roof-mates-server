package labs.haint.repo

import labs.haint.data.Region
import labs.haint.data.Spot
import labs.haint.dummy.dummyRegions
import labs.haint.dummy.dummySpots

interface RegionsRepository {
    suspend fun all(): List<Region>
    suspend fun save(region: Region)
    suspend fun save(spot: Spot)
    suspend fun byId(id: Long): Region?
    suspend fun spots(regionId: Long): List<Spot>
}

class InMemoryRegionsRepository(
    private val regions: MutableList<Region> = dummyRegions,
    private val spots: MutableList<Spot> = dummySpots,
) : RegionsRepository {
    private var regionId = dummyRegions.count().toLong()
    private var spotId = dummySpots.count().toLong()

    override suspend fun all(): List<Region> = regions
        .map { region -> region.copy(spots = spots.filter { it.regionId == region.id }) }

    override suspend fun save(region: Region) {
        regions.find { it.address == region.address }?.let {
            throw Error("Region with same address already exists")
        }

        regions += region.copy(id = regionId++)
    }

    override suspend fun save(spot: Spot) {
        spots.find { it.number == spot.number }?.let {
            throw Error("Spot with same number already exists")
        }

        spots += spot.copy(id = spotId++)
    }

    override suspend fun byId(id: Long): Region? = regions.find { it.id == id }

    override suspend fun spots(regionId: Long): List<Spot> {
        return spots.filter { it.regionId == regionId }
    }
}