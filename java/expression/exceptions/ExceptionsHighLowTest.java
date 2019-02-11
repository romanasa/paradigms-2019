package expression.exceptions;

import java.util.Arrays;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class ExceptionsHighLowTest extends ExceptionsTest {
    protected ExceptionsHighLowTest() {
        unary.add(op("high", ExceptionsHighLowTest::high));
        unary.add(op("low", ExceptionsHighLowTest::low));

        tests.addAll(Arrays.asList(
                op("high -4", (x, y, z) -> Integer.MIN_VALUE),
                op("high-5", (x, y, z) -> Integer.MIN_VALUE),
                op("low 4", (x, y, z) -> 4),
                op("low 18", (x, y, z) -> 2),
                op("low x * y * z", (x, y, z) -> low(x) * y * z),
                op("low(x * y * z)", (x, y, z) -> low(x * y * z)),
                op("high(x + y + z)", (x, y, z) -> high(x + y + z))
        ));
        parsingTest.addAll(Arrays.asList(
                op("hello", "hello"),
                op("high", "high"),
                op("high()", "high()"),
                op("high(1, 2)", "high(1, 2)"),
                op("abb 1", "abb 1"),
                op("abb 1", "abb 1"),
                op("high *", "high *"),
                op("highx", "highx"),
                op("highx 10", "highx 10"),
                op("lоw 4", "lоw 4")
        ));
    }

    private static long high(final long v) {
        return Integer.highestOneBit((int) v);
    }

    private static long low(final long v) {
        return Integer.lowestOneBit((int) v);
    }

    public static void main(final String[] args) {
        new ExceptionsHighLowTest().run();
    }
}
