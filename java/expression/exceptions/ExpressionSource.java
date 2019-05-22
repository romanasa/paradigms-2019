package expression.exceptions;

import java.io.IOException;

public abstract class ExpressionSource {
    public static char END = '\0';

    protected int pos;
    protected int line;
    protected int posInLine;
    private char c;

    protected abstract char readChar() throws IOException;
    protected abstract String rest();

    public char getChar() {
        return c;
    }

    public char nextChar() throws ParseException {
        try {
            if (c == '\n') {
                line++;
                posInLine = 0;
            }
            c = readChar();
            pos++;
            posInLine++;
            return c;
        } catch (IOException e) {
            throw error("Source read error", e.getMessage());
        }
    }

    public ParseException error(final String format, final Object ...args) throws ParseException {
        throw new ParseException(line, pos, String.format(format, args));
    }
}
