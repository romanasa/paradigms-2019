package expression.exceptions;

import expression.TripleExpression;

public class CheckedAdd extends AbstractOperator {
    public CheckedAdd(TripleExpression first, TripleExpression second) {
        super(first, second);
    }

    public int operator(int a, int b) throws Exception {
        if (a < 0 && b < 0 && Integer.MIN_VALUE - a > b) {
            throw new Exception("Integer overflow: (" + a + ") + (" + b + ")");
        }
        if (a > 0 && b > 0 && a > Integer.MAX_VALUE - b) {
            throw new Exception("Integer overflow: (" + a + ") + (" + b + ")");
        }
        return a + b;
    }
}
