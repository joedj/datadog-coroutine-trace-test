import datadog.trace.api.GlobalTracer
import datadog.trace.api.Trace
import datadog.trace.context.TraceScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TraceTest {

    @Trace
    suspend fun suspending(): Pair<String, String> {
        val tracer = GlobalTracer.get()
        val traceId1 = tracer.traceId
        delay(1)
        val traceId2 = tracer.traceId
        return traceId1 to traceId2
    }

    @Test
    fun `trace annotated suspend function`(): Unit = runBlocking {
        // when
        val (traceId1, traceId2) = suspending()
        // then
        assertNotNull(traceId1)
        assertNotEquals(traceId1, "")
        assertNotEquals(traceId1, "0")
        assertEquals(traceId1, traceId2)
    }

    @Test
    fun `manually trace suspend function`(): Unit = runBlocking {
        val tracer = io.opentracing.util.GlobalTracer.get()
        val span = tracer.buildSpan("test").start()
        tracer.activateSpan(span).use { scope ->
            if (scope is TraceScope) {
                scope.setAsyncPropagation(true)
            }

            // when
            val (traceId1, traceId2) = suspending()

            // then
            assertNotNull(traceId1)
            assertNotEquals(traceId1, "")
            assertNotEquals(traceId1, "0")
            assertEquals(traceId1, traceId2)
        }
    }

}
