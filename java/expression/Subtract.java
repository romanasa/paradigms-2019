package expression;

public class Subtract extends AbstractOperator {
    public Subtract(TripleExpression first, TripleExpression second) {
        super(first, second);
    }

    public int operator(int a, int b) {
        return a - b;
    }
}