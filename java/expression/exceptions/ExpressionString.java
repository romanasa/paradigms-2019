package expression.exceptions;

class ExpressionString {
    private String s;
    private int pos;

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(s);
        out.append('\n');
        for (int i = 0; i < pos; i++) out.append('-');
        out.append('^');
        for (int i = pos + 1; i < s.length(); i++) out.append('-');
        return (isEmpty() ? "end of string" : s.substring(pos)) + "\n" + out;
    }

    ExpressionString(String s) {
        this.s = s;
        this.pos = 0;
        skip();
    }

    char first() {
        return s.charAt(pos);
    }

    char second() {
        return s.charAt(pos + 1);
    }

    private void skip() {
        while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
            pos++;
        }
    }

    void removeFirst() {
        pos++;
        skip();
    }

    void removeFirst(int len) {
        pos += len;
        skip();
    }

    boolean isEmpty() {
        return pos >= s.length();
    }

    boolean hasTwo() {
        return pos + 1 < s.length();
    }

    boolean contains(String operator) {
        return has(operator) && (pos + operator.length() == s.length() ||
                !Character.isLetter(s.charAt(pos + operator.length())));
    }

    private boolean has(String operator) {
        if (pos + operator.length() <= s.length()) {
            for (int i = 0; i < operator.length(); i++) {
                if (s.charAt(pos + i) != operator.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
