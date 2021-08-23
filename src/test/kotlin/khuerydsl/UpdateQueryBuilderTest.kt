package khuerydsl

import testentity.TestEntity
import testentity.TestEnum
import com.sia.khuerydsl.builder.UpdateQueryBuilder
import com.sia.khuerydsl.exception.MissingSetExpressionException
import com.sia.khuerydsl.exception.MissingWhereExpressionException
import hibernate.HibernateManager
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import org.hibernate.Session
import java.sql.Timestamp
import java.time.Instant

class UpdateQueryBuilderTest : FunSpec({

    val parameters = mutableListOf<Any>()
    var builder: UpdateQueryBuilder<TestEntity>? = null
    val mocking = false // Session을 mocking할지 여부를 결정하는 변수(mocking시 문자열만 검사)
    val session: Session = if (mocking) {
        getMockSession(parameters, null, null)
    } else {
        HibernateManager.initSessionFactory()
        HibernateManager.getSession()
    }

    beforeTest {
        builder = UpdateQueryBuilder(session, TestEntity::class)
    }

    afterTest {
        parameters.clear()
    }

    test("UPDATE TestEntity SET number = :Number WHERE id = :Id") {
        val values = listOf(15.0, 1)
        builder = builder!!
            .set { TestEntity::number.eq(values[0]) }
            .where { TestEntity::id.eq(values[1]) }

        val queryString = builder!!.getQueryString()
        queryString shouldBe "UPDATE TestEntity SET number = :Number WHERE id = :Id"

        if (mocking) {
            for (i in values.indices) {
                builder!!.build()
                values[i] shouldBeIn parameters
            }
        } else {
            val tx = session.transaction
            tx.begin()
            builder!!.build().executeUpdate()
            tx.commit()
        }
    }

    test("UPDATE TestEntity SET dateTime = :DateTime WHERE id = :Id OR (number BETWEEN :Number1 AND :Number2) AND (enum != :Enum AND name IN :Name)") {
        val values = listOf(Timestamp.from(Instant.now()), 1, 5.0, 10.0, TestEnum.CCCC, listOf("hello", "world"))
        builder = builder!!
            .set { TestEntity::dateTime.eq(values[0]) }
            .where {
                TestEntity::id.eq(values[1]) or {
                    fieldOf(TestEntity::number).between(values[2]).and(values[3])
                } and {
                    fieldOf(TestEntity::enum).neq(values[4]) and fieldOf(TestEntity::name).inList(values[5] as List<*>)
                }
            }

        val queryString = builder!!.getQueryString()
        queryString shouldBe "UPDATE TestEntity SET dateTime = :DateTime WHERE id = :Id OR (number BETWEEN :Number1 AND :Number2) AND (enum != :Enum AND name IN :Name)"

        if (mocking) {
            for (i in values.indices) {
                builder!!.build()
                values[i] shouldBeIn parameters
            }
        } else {
            val tx = session.transaction
            tx.begin()
            builder!!.build().executeUpdate()
            tx.commit()
        }
    }

    test("where expression이 없을 때는 예외가 발생해야 한다") {
        shouldThrow<MissingWhereExpressionException> {
            builder!!
                .set { TestEntity::id.eq(0) }
                .build()
        }
    }

    test("set expression이 없을 때는 예외가 발생해야 한다") {
        shouldThrow<MissingSetExpressionException> {
            builder!!
                .where { TestEntity::id.eq(0) }
                .build()
        }
    }
})
