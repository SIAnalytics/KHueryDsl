package testentity

import psql.PostgresDoubleListType
import psql.PostgresIntListType
import psql.PostgresStringListType
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import org.locationtech.jts.geom.MultiPolygon
import java.sql.Timestamp
import javax.persistence.*

@Entity
@TypeDefs(
    TypeDef(name = "IntList", typeClass = PostgresIntListType::class),
    TypeDef(name = "StringList", typeClass = PostgresStringListType::class),
    TypeDef(name = "pSqlEnum", typeClass = PostgreSQLEnumType::class),
    TypeDef(name = "DoubleList", typeClass = PostgresDoubleListType::class)
)
data class TestEntity(
    @Id
    @GeneratedValue
    var id: Int? = null,
    var name: String,
    var number: Double,
    @Type(type = "IntList")
    var intList: List<Int>,
    @Type(type = "DoubleList")
    var doubleList: List<Double>,
    @Type(type = "StringList")
    var stringList: List<String>,
    @Column(columnDefinition = "geography(MultiPolygon, 4326)")
    var multiPolygon: MultiPolygon,
    val dateTime: Timestamp,
    @OneToOne
    @JoinColumn(name = "sub_entity_id_fkey")
    var subEntity: SubEntity? = null,
    @Enumerated(EnumType.STRING)
    @Type(type = "pSqlEnum")
    val enum: TestEnum
) {
    fun change(
        name: String? = null,
        number: Double? = null,
        intList: List<Int>? = null,
        doubleList: List<Double>? = null,
        stringList: List<String>? = null,
        multiPolygon: MultiPolygon? = null,
        dateTime: Timestamp? = null,
        subEntity: SubEntity? = null,
        enum: TestEnum? = null
    ): TestEntity {
        val newName = name ?: this.name
        val newNumber = number ?: this.number
        val newIntList = intList ?: this.intList
        val newDoubleList = doubleList ?: this.doubleList
        val newStringList = stringList ?: this.stringList
        val newMultiPolygon = multiPolygon ?: this.multiPolygon
        val newDateTime = dateTime ?: this.dateTime
        val newSubEntity = subEntity ?: this.subEntity
        return TestEntity(
            id = this.id,
            name = newName,
            number = newNumber,
            intList = newIntList,
            doubleList = newDoubleList,
            stringList = newStringList,
            multiPolygon = newMultiPolygon,
            dateTime = newDateTime,
            subEntity = newSubEntity,
            enum = enum ?: this.enum
        )
    }
}
