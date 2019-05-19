package expression.exceptions;

import expression.TripleExpression;

public class CheckedDivide extends AbstractOperator {
    public CheckedDivide(TripleExpression first, TripleExpression second) {
        super(first, second);
    }

    public int operator(int a, int b) throws Exception {
        if (a == Integer.MIN_VALUE && b == -1) {
            throw new Exception("Integer overflow: (" + a + ") * (" + b + ")");
        }
        if (b == 0) {
            throw new Exception("Division by zero");
        }
        return a / b;
    }
}
