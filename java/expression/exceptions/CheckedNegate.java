package expression.exceptions;

import expression.TripleExpression;

public class CheckedNegate extends AbstractOperator {
    public CheckedNegate(TripleExpression first) {
        super(first, first);
    }

    public int operator(int a, int b) throws Exception {
        if (b == Integer.MIN_VALUE) {
            throw new Exception("Integer overflow: -(" + b + ")");
        }
        return -b;
    }
}
