package expression;

public class Variable implements TripleExpression {
    private String name;

    public Variable(String name) {
        this.name = name;
    }

    public int evaluate(int x, int y, int z) {
        switch (name) {
            case "x":
                return x;
            case "y":
                return y;
            default:
                assert (name.equals("z"));
                return z;
        }
    }
}