package expression.exceptions;

public class ParseException extends Exception {
    private final int line;
    private final int pos;

    public ParseException(int line, int pos, final String message) {
        super(message);
        this.line = line;
        this.pos = pos;
    }
}