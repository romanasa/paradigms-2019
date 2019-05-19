package expression.exceptions;

import expression.TripleExpression;

public class CheckedHigh extends AbstractOperator {
    public CheckedHigh(TripleExpression first) {
        super(first, first);
    }

    public int operator(int a, int b) throws Exception {
        if (a < 0) {
            return Integer.MIN_VALUE;
        }
        for (int i = 31; i >= 0; i--) {
            if (((a >> i) & 1) != 0) {
                return 1 << i;
            }
        }
        return 0;
    }
}
