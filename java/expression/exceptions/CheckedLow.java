package expression.exceptions;

import expression.TripleExpression;

public class CheckedLow extends AbstractOperator {
    public CheckedLow(TripleExpression first) {
        super(first, first);
    }

    public int operator(int a, int b) throws Exception {
        return a - (a & (a - 1));
    }
}
