package md2html;

import java.io.IOException;

public class Const implements StringExpression {
    private String s;
    private Md2HtmlSource source;
    Const(String x, Md2HtmlSource y) {
        s = x;
        source = y;
    }

    public void evaluate() throws IOException {
        source.writeString(s);
    }

}