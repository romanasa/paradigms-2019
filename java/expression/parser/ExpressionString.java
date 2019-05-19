package expression.parser;

class ExpressionString {
    private String s;
    private int pos;

    @Override
    public String toString() {
        return s.substring(pos);
    }

    ExpressionString(String s) {
        this.s = s;
        this.pos = 0;
    }

    char first() {
        return s.charAt(pos);
    }

    void removeFirst() {
        pos++;
    }

    boolean isEmpty() {
        return pos >= s.length();
    }
}
