package testentity

import com.github.debop.kodatimes.now
import com.github.debop.kodatimes.toTimestamp
import psql.PostgresTimestampListType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.sql.Timestamp
import javax.persistence.Entity
import javax.persistence.Id

@Entity(name = "timestamps")
@TypeDef(name = "TimestampList", typeClass = PostgresTimestampListType::class)
data class TimestampEntity(
    @Id
    val id: Int,
    val time: Timestamp = now().toTimestamp(),
    @Type(type = "TimestampList")
    val times: List<Timestamp>
)
