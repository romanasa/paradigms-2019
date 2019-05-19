package expression;

public class Add extends AbstractOperator {
    public Add(TripleExpression first, TripleExpression second) {
        super(first, second);
    }

    public int operator(int a, int b) {
        return a + b;
    }
}