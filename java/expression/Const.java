package expression;

public class Const implements TripleExpression {
    private Object value;

    public Const(Object x) {
        value = x;
    }

    public int evaluate(int x, int y, int z) {
        return (int)value;
    }
}