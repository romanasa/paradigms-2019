package expression.exceptions;

import expression.TripleExpression;

public class CheckedMultiply extends AbstractOperator {
    public CheckedMultiply(TripleExpression first, TripleExpression second) {
        super(first, second);
    }


    private boolean bad(int a, int b, int c) {
        int maxa = c / (b < 0 ? -b : b);
        return a <= maxa;
    }

    private int sign(int x) {
        return x <= 0 ? -1 : 1;
    }

    public int operator(int a, int b) throws Exception {
        if (a > b) {
            int t = a ^ b;
            a = t ^ a;
            b = t ^ a;
        }
        if (a == 0 || b == 0 || a == 1) {
            return a * b;
        }
        if (b == -1) {
            if (a == Integer.MIN_VALUE) {
                throw new Exception("Integer overflow: (" + a + ") * (" + b + ")");
            } else {
                return a * b;
            }
        }
        if (a * b / b != a) {
            throw new Exception("Integer overflow: (" + a + ") * (" + b + ")");
        }
        return a * b;
    }
}
