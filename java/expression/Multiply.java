package expression;

public class Multiply extends AbstractOperator {
    public Multiply(TripleExpression first, TripleExpression second) {
        super(first, second);
    }

    public int operator(int a, int b) {
        return a * b;
    }
}