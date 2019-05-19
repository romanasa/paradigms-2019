package expression;

public class Divide extends AbstractOperator {
    public Divide(TripleExpression first, TripleExpression second) {
        super(first, second);
    }

    public int operator(int a, int b) {
        return a / b;
    }
}