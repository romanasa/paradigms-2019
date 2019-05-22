package expression.operations;

public class FloatOperation implements Operation<Float> {

    public Float add(Float x, Float y) {
        return x + y;
    }

    public Float sub(Float x, Float y) {
        return x - y;
    }

    public Float mul(Float x, Float y) {
        return x * y;
    }

    public Float div(Float x, Float y) {
        return x / y;
    }

    public Float mod(Float x, Float y) {
        return x % y;
    }

    public Float abs(Float x) {
        return Math.abs(x);
    }

    public Float square(Float x) {
        return x * x;
    }

    public Float neg(Float x) {
        return -x;
    }

    public Float parseNumber(String s) throws NumberException {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            throw new NumberException("Wrong Float number: " + s);
        }
    }
}
