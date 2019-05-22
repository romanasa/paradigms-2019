package expression.exceptions;

import java.io.IOException;

public class StringExpressionSource extends ExpressionSource {
    private String source;

    public StringExpressionSource(String source) {
        this.source = source + END;
    }

    @Override
    protected char readChar() throws IOException {
        return source.charAt(pos);
    }

    protected String rest() {
        int oldPos = pos;

        pos = Math.min(this.source.length(), pos + 5);
        while (pos < this.source.length() && !Character.isWhitespace(source.charAt(pos))) {
            pos++;
        }

        return (oldPos == this.source.length() ? "end of text" : source.substring(oldPos - 1, pos)) + "'\n" +
                source + "\n" + "-".repeat(oldPos - 1) + "^" + "-".repeat(source.length() - oldPos);
    }
}
