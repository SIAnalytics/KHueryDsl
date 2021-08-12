package khuerydsl

import fixture.getMultiPolygon
import testentity.TestEntity
import testentity.TestEnum
import com.sia.khuerydsl.builder.SearchQueryBuilder
import com.sia.khuerydsl.builder.SelectExpressionBuilder.avg
import com.sia.khuerydsl.builder.SelectExpressionBuilder.count
import com.sia.khuerydsl.builder.SelectExpressionBuilder.max
import com.sia.khuerydsl.builder.SelectExpressionBuilder.min
import com.sia.khuerydsl.builder.SelectExpressionBuilder.sum
import com.sia.khuerydsl.builder.SortDirection
import com.sia.khuerydsl.exception.SameNamedParameterException
import hibernate.HibernateManager
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import io.mockk.slot
import org.hibernate.Session
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.PrecisionModel
import java.time.LocalDate

class SearchQueryBuilderTest : FunSpec({

    val parameters = mutableListOf<Any>()
    val limitSlot = slot<Int>()
    val offsetSlot = slot<Int>()
    var builder: SearchQueryBuilder<TestEntity>? = null
    val mocking = false // Session을 mocking할지 여부를 결정하는 변수(mocking시 문자열만 검사)
    val session: Session = if (mocking) {
        getMockSession(parameters, limitSlot, offsetSlot)
    } else {
        HibernateManager.initSessionFactory()
        HibernateManager.getSession()
    }

    beforeTest {
        builder = SearchQueryBuilder(session, TestEntity::class)
            .from(TestEntity::class)
    }

    afterTest {
        parameters.clear()
    }

    // DISTINCT
    test("SELECT DISTINCT T FROM TestEntity T") {
        builder!!.from(TestEntity::class)
            .select()
            .distinct()

        val queryString = builder!!.getQueryString()
        queryString shouldBe "SELECT DISTINCT T FROM TestEntity T"

        builder!!.build().resultList
    }

    test("SELECT DISTINCT T.name, T.id FROM TestEntity T") {
        builder!!.from(TestEntity::class)
            .select(TestEntity::name, TestEntity::id)
            .distinct()

        val queryString = builder!!.getQueryString()
        queryString shouldBe "SELECT DISTINCT T.name, T.id FROM TestEntity T"

        builder!!.build().resultList
    }

    test("SELECT DISTINCT COUNT(*), MIN(T.id), T.number FROM TestEntity T GROUP BY T.number") {
        builder!!.from(TestEntity::class)
            .select(TestEntity::class.count(), TestEntity::id.min(), TestEntity::number)
            .distinct()
            .groupBy(TestEntity::number)

        val queryString = builder!!.getQueryString()
        queryString shouldBe "SELECT DISTINCT COUNT(*), MIN(T.id), T.number FROM TestEntity T GROUP BY T.number"

        builder!!.build().resultList
    }

    test("SELECT DISTINCT * FROM test_entity WHERE ST_Intersects(multi_polygon, :MultiPolygon)") {
        val value = getMultiPolygon()
        builder!!.from(TestEntity::class)
            .select()
            .distinct()
            .where { st_intersects(TestEntity::multiPolygon, value) }

        val queryString = builder!!.getQueryString()
        queryString shouldBe "SELECT DISTINCT * FROM test_entity WHERE ST_Intersects(multi_polygon, :MultiPolygon)"

        builder!!.build().resultList
        if (mocking) {
            value shouldBeIn parameters
        }
    }

    // 집계함수 테스트
    test("SELECT COUNT(*) FROM TestEntity T") {
        builder!!.from(TestEntity::class)
            .select(TestEntity::class.count())

        val queryString = builder!!.getQueryString()
        queryString shouldBe "SELECT COUNT(*) FROM TestEntity T"

        builder!!.build().resultList
    }

    test("SELECT SUM(T.id), AVG(T.id), MAX(T.id), MIN(T.id) FROM TestEntity T") {
        builder!!.from(TestEntity::class)
            .select(TestEntity::id.sum(), TestEntity::id.avg(), TestEntity::id.max(), TestEntity::id.min())

        val queryString = builder!!.getQueryString()
        queryString shouldBe "SELECT SUM(T.id), AVG(T.id), MAX(T.id), MIN(T.id) FROM TestEntity T"

        builder!!.build().resultList
    }

    test("SELECT COUNT(T.id), T.name FROM TestEntity T GROUP BY T.name") {
        builder!!.from(TestEntity::class)
            .select(TestEntity::id.count(), TestEntity::name)
            .groupBy(TestEntity::name)

        val queryString = builder!!.getQueryString()
        queryString shouldBe "SELECT COUNT(T.id), T.name FROM TestEntity T GROUP BY T.name"

        builder!!.build().resultList
    }

    test("FROM TestEntity WHERE id = :Id OR (enum = :Enum AND number IN :Number)") {
        val values = listOf(5, TestEnum.AAAA, listOf(1.0, 2.0, 3.0))
        builder!!.from(TestEntity::class)
            .where {
                TestEntity::id.eq(values[0]) or {
                    TestEntity::enum.eq(values[1]) and TestEntity::number.inList(values[2] as List<*>)
                }
            }

        val queryString = builder!!.getQueryString()
        queryString shouldBe "FROM TestEntity WHERE id = :Id OR (enum = :Enum AND number IN :Number)"

        builder!!.build().resultList
        if (mocking) {
            for (i in values.indices) {
                values[i] shouldBeIn parameters
            }
        }
    }

    test("SELECT id, name FROM test_entity WHERE ST_Intersects(multi_polygon, :MultiPolygon)") {
        val value = getMultiPolygon()
        builder!!.from(TestEntity::class)
            .select(TestEntity::id, TestEntity::name)
            .where { st_intersects(TestEntity::multiPolygon, value) }

        val queryString = builder!!.getQueryString()
        queryString shouldBe "SELECT id, name FROM test_entity WHERE ST_Intersects(multi_polygon, :MultiPolygon)"

        builder!!.build().resultList
        if (mocking) {
            value shouldBeIn parameters
        }
    }

    test("SELECT * FROM test_entity WHERE ST_Intersects(multi_polygon, :MultiPolygon) AND date_time <= :DateTime") {
        val values = listOf(getMultiPolygon(), LocalDate.now())
        builder!!.from(TestEntity::class)
            .where { st_intersects(TestEntity::multiPolygon, values[0]) and fieldOf(TestEntity::dateTime).lessThan(values[1]) }

        val queryString = builder!!.getQueryString()
        queryString shouldBe "SELECT * FROM test_entity WHERE ST_Intersects(multi_polygon, :MultiPolygon) AND date_time <= :DateTime"

        builder!!.build().resultList
        if (mocking) {
            for (i in values.indices) {
                values[i] shouldBeIn parameters
            }
        }
    }

    test("SELECT * FROM test_entity WHERE ST_DWithin(ST_Transform(cast(multi_polygon AS geometry), 2097), ST_Transform(:Point, 2097), :Radius) OR name = :Name OR name LIKE :Name") {
        val values = listOf(GeometryFactory(PrecisionModel(PrecisionModel.FLOATING), 2097).createPoint(Coordinate(1.0, 1.0)), 1.0, 1, "testName")
        builder!!.from(TestEntity::class)
            .where {
                st_dwithin(TestEntity::multiPolygon, values[0] as Point?, values[1] as Double?) or
                    fieldOf(TestEntity::id).eq(values[2]) or
                    fieldOf(TestEntity::name).like(values[3] as String?)
            }

        val queryString = builder!!.getQueryString()
        queryString shouldBe "SELECT * FROM test_entity WHERE ST_DWithin(ST_Transform(cast(multi_polygon AS geometry), 2097), ST_Transform(:Point, 2097), :Radius) OR id = :Id OR name LIKE :Name"

        builder!!.build().resultList
        if (mocking) {
            for (i in values.indices) {
                values[i] shouldBeIn parameters
            }
        }
    }

    test("FROM TestEntity") {
        val queryString = builder!!.from(TestEntity::class)
            .getQueryString()
        queryString shouldBe "FROM TestEntity"

        builder!!.build().resultList
    }

    test("FROM TestEntity WHERE id = :Id") {
        val value = 1
        builder!!.where { fieldOf(TestEntity::id) eq value }

        val queryString = builder!!.getQueryString()
        queryString shouldBe "FROM TestEntity WHERE id = :Id"

        builder!!.build().resultList
        if (mocking) {
            value shouldBe parameters[0]
        }
    }

    test("FROM TestEntity WHERE id != :Id") {
        val value = 1
        builder!!.where { TestEntity::id.neq(value) }

        val queryString = builder!!.getQueryString()
        queryString shouldBe "FROM TestEntity WHERE id != :Id"

        builder!!.build().resultList
        if (mocking) {
            value shouldBe parameters[0]
        }
    }

    test("FROM TestEntity WHERE id != :Id OR number BETWEEN :Number1 AND :Number2") {
        val values = listOf(1, 1.0, 2.0)
        builder!!.where { TestEntity::id.neq(values[0]) or TestEntity::number.between(values[1]).and(values[2]) }

        val queryString = builder!!.getQueryString()
        queryString shouldBe "FROM TestEntity WHERE id != :Id OR number BETWEEN :Number1 AND :Number2"

        builder!!.build().resultList
        if (mocking) {
            for (i in values.indices) {
                values[i] shouldBeIn parameters
            }
        }
    }

    test("FROM TestEntity ORDER BY id ASC") {
        builder!!.orderBy(TestEntity::id, SortDirection.ASC)

        val queryString = builder!!.getQueryString()
        queryString shouldBe "FROM TestEntity ORDER BY id ASC"

        builder!!.build().resultList
    }

    test("SELECT T.id, T.name FROM TestEntity T") {
        builder!!.from(TestEntity::class)
            .select(TestEntity::id, TestEntity::name)

        val queryString = builder!!.getQueryString()
        queryString shouldBe "SELECT T.id, T.name FROM TestEntity T"

        builder!!.build().resultList
    }

    test("Query에 limit값이 설정되어야 한다(mockSession을 이용하는 경우에만 테스트 가능)") {
        val value = 10
        builder!!.limit(value)
            .build().resultList

        if (mocking) {
            limitSlot.captured shouldBe value
        }
    }

    test("Query에 offset값이 설정되어야 한다(mockSession을 이용하는 경우에만 테스트 가능)") {
        val value = 22
        builder!!.offset(value)
            .build().resultList

        if (mocking) {
            offsetSlot.captured shouldBe value
        }
    }

    test("and 또는 or 사용시 중괄호로 감싸면 괄호가 추가되어야 한다") {
        val values = listOf(1, 2, 3, 4, 5, 6, 7)
        builder!!.from(TestEntity::class)
            .where {
                TestEntity::id.eq(values[0]) and {
                    TestEntity::number.lessThan(values[1]) and {
                        TestEntity::id.eq(values[2]) or TestEntity::name.eq(values[3])
                    } or {
                        TestEntity::id.eq(values[4]) and TestEntity::id.eq(values[5])
                    } and TestEntity::id.eq(values[6])
                }
            }

        val queryString = builder!!.getQueryString()
        // 괄호가 잘 생성되는지만을 검사하기 위한 테스트로 문자열만 검사(DB와 연동하여 쿼리를 실행할 경우 에러가 발생)
        queryString shouldBe "FROM TestEntity WHERE id = :Id AND (number <= :Number AND (id = :Id OR name = :Name) OR (id = :Id AND id = :Id) AND id = :Id)"
    }

    test("같은 이름의 named parameter가 두 번 이상 등장하면 예외가 발생해야 한다(Session을 이용하는 경우에만 테스트 가능)") {
        if (!mocking) {
            shouldThrow<SameNamedParameterException> {
                val values = listOf("test1", "test2")
                builder!!.from(TestEntity::class)
                    .where {
                        TestEntity::name.eq(values[0]) and
                            TestEntity::name.neq(values[1])
                    }

                builder!!.build()
            }
        }
    }
})
