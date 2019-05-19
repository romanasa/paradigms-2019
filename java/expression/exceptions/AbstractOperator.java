package expression.exceptions;

import expression.TripleExpression;

public abstract class AbstractOperator implements TripleExpression {

    private TripleExpression first, second;

    public AbstractOperator(TripleExpression first, TripleExpression second) {
        this.first = first;
        this.second = second;
    }

    public abstract int operator(int first, int second) throws Exception;

    public int evaluate(int x, int y, int z) throws Exception {
        return operator(first.evaluate(x, y, z), second.evaluate(x, y, z));
    }
}