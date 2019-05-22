package md2html;

import java.io.IOException;

public class Concatenate implements StringExpression {
    private StringExpression f, s;

    Concatenate(StringExpression a, StringExpression b) {
        f = a;
        s = b;
    }

    public void evaluate() throws IOException {
        f.evaluate();
        s.evaluate();
    }
}