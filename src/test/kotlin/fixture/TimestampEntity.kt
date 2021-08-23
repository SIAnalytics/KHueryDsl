package fixture

import com.github.debop.kodatimes.now
import com.github.debop.kodatimes.toTimestamp
import testentity.TimestampEntity

fun getTimestampEntity() = TimestampEntity(
    id = 3456,
    times = listOf(now().toTimestamp(), (now() + 44555L).toTimestamp())
)

fun getTimestampEntity1() = TimestampEntity(
    id = 3443,
    times = listOf(now().toTimestamp(), (now() + 44555L).toTimestamp()),
    time = (now() - 44555L).toTimestamp()
)

fun getTimestampEntity2() = TimestampEntity(
    id = 4456,
    times = listOf(now().toTimestamp(), (now() + 44555L).toTimestamp()),
    time = (now() + 44555L).toTimestamp()
)
