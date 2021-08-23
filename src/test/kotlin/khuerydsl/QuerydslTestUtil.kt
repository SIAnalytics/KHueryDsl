package khuerydsl

import testentity.TestEntity
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.hibernate.Session
import org.hibernate.query.NativeQuery
import org.hibernate.query.Query

fun getMockSession(parameters: MutableList<Any>, limitSlot: CapturingSlot<Int>?, offsetSlot: CapturingSlot<Int>?): Session {
    val mockSession: Session = mockk()
    val mockQuery: Query<TestEntity> = mockk()
    val mockNativeQuery: NativeQuery<TestEntity> = mockk()
    val parameterNameSlot: CapturingSlot<String> = slot()

    every { mockQuery.setParameter(any<String>(), capture(parameters)) } returns mockQuery
    every { mockQuery.resultList } returns mockk()
    every { mockQuery.getParameterValue(capture(parameterNameSlot)) } throws IllegalStateException()
    every { mockQuery.getParameter(any<String>()) } returns mockk()
    every { mockQuery.isBound(any()) } returns false
    every { mockNativeQuery.setParameter(any<String>(), capture(parameters)) } returns mockNativeQuery
    every { mockNativeQuery.resultList } returns mockk()
    every { mockNativeQuery.getParameterValue(capture(parameterNameSlot)) } throws IllegalStateException()
    every { mockNativeQuery.getParameter(any<String>()) } returns mockk()
    every { mockNativeQuery.isBound(any()) } returns false
    every { mockSession.createQuery<TestEntity>(any(), any()) } returns mockQuery
    every { mockSession.createQuery(any<String>()) } returns mockQuery
    every { mockSession.createNativeQuery(any(), any<Class<Any>>()) } returns mockNativeQuery
    limitSlot?.let { every { mockQuery.setMaxResults(capture(limitSlot)) } returns mockQuery }
    offsetSlot?.let { every { mockQuery.setFirstResult(capture(offsetSlot)) } returns mockQuery }

    return mockSession
}
