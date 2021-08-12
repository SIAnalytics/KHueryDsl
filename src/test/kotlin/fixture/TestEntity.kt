package fixture

import com.github.debop.kodatimes.toTimestamp
import testentity.TestEntity
import testentity.TestEnum
import org.joda.time.DateTime
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory

fun getTestEntity1() = TestEntity(
    name = "test1",
    number = 0.1,
    intList = listOf(1, 2, 3),
    doubleList = listOf(0.1, 0.22, 0.333),
    stringList = listOf("ab", "cd", "ef"),
    multiPolygon = GeometryFactory().createMultiPolygon(getPolygons()),
    dateTime = DateTime(2019, 12, 31, 15, 50).toTimestamp(),
    enum = TestEnum.AAAA
)

fun getTestEntity2() = TestEntity(
    name = "test2",
    number = 0.2,
    intList = listOf(1, 2, 3),
    doubleList = listOf(0.1, 0.22, 0.333),
    stringList = listOf("ab1", "cd1", "ef1"),
    multiPolygon = GeometryFactory().createMultiPolygon(getPolygons()),
    dateTime = DateTime(2019, 12, 31, 15, 50).toTimestamp(),
    enum = TestEnum.BBBB
)

fun getTestEntity3() = TestEntity(
    name = "test3",
    number = 0.3,
    intList = listOf(1, 2, 3),
    doubleList = listOf(0.1, 0.22, 0.333),
    stringList = listOf("ab2", "cd2", "ef2"),
    multiPolygon = GeometryFactory().createMultiPolygon(getPolygons()),
    dateTime = DateTime(2019, 12, 31, 15, 50).toTimestamp(),
    enum = TestEnum.CCCC
)

fun getCoordinates1() = arrayOf(
    Coordinate(124.0, 38.0),
    Coordinate(128.0, 38.0),
    Coordinate(128.0, 41.0),
    Coordinate(124.0, 41.0),
    Coordinate(124.0, 38.0)
)

fun getCoordinates2() = arrayOf(
    Coordinate(130.0, 42.0),
    Coordinate(131.0, 42.0),
    Coordinate(131.0, 43.0),
    Coordinate(130.0, 43.0),
    Coordinate(130.0, 42.0)
)

fun getPolygons() = arrayOf(GeometryFactory().createPolygon(getCoordinates1()))

fun getPolygon() = GeometryFactory().createPolygon(getCoordinates2())

fun getMultiPolygon() = GeometryFactory().createMultiPolygon(
    getPolygons()
)
