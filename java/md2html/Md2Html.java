package md2html;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Md2Html {
    private static Md2HtmlSource source;
    private static Map<Character, String> symbols = Map.of(
            '<', "&lt;",
            '>', "&gt;",
            '&', "&amp;"
    );

    private static Map<Character, String> tags = Map.of(
            '*', "em",
            '_', "em",
            '`', "code",
            '~', "mark",
            '-', "s"
    );

    public static void main(String[] args) throws IOException {
        new Md2Html().parse(args);
    }

    private void parse(String[] args) throws IOException {
        source = new Md2HtmlSource(args[0], args[1]);
        source.nextChar();
        source.skipSeparators();

        while (source.getChar() != Md2HtmlSource.END) {
            parseParagraph();
            source.skipSeparators();
        }
        source.close();
    }

    private void parseParagraph() throws IOException {
        int depth = 0;
        while (source.getChar() == '#') {
            depth++;
            source.nextChar();
        }

        StringExpression curRes;
        if (source.getChar() != ' ' || depth == 0) {
            Const c = new Const("#".repeat(depth), source);
            StringExpression result = parseLine("");
            curRes = new Tag(new Concatenate(c, result), "p", source);
        } else {
            source.nextChar();
            StringExpression s = parseLine("");
            curRes = new Tag(s, "h" + depth, source);
        }
        curRes.evaluate();
        new Const(System.lineSeparator(), source).evaluate();
    }

    private StringExpression parseSimpleLine(boolean find) throws IOException {
        StringExpression cur = new Const("", source);

        while (!source.EOLn() && notTest('_', '-', '*', '`', '+', '~') && (!find || notTest(']', ')')) && notTest('!', '[')) {
            if (test('<', '>', '&')) {
                cur = new Concatenate(cur, parseLiteral(false));
                source.nextChar();
            } else if (testNext('\\')) {
                cur = new Concatenate(cur, parseSymbol());
                source.nextChar();
            } else {
                if (source.getChar() != '\r') {
                    cur = new Concatenate(cur, new Const(Character.toString(source.getChar()), source));
                }
                source.nextChar();
            }
        }
        return cur;
    }

    private StringExpression parseLine(String block) throws IOException {
        StringExpression cur = new Const("", source);
        int ind = 0;

        while ((source.getChar() != Md2HtmlSource.END) && (source.getSeparators() < 2) &&
                (block.isEmpty() || (ind < block.length()))) {
            char type = source.getChar();
            if (test('*', '_', '-', '+')) {
                cur = new Concatenate(cur, parseDoubleHighlight());
            } else if (testNext('`') || testNext('~')) {
                cur = new Concatenate(cur, parseHighlight(type));
            } else if (testNext('[')) {
                cur = new Concatenate(cur, parseLink());
            } else if (testNext('!')) {
                cur = new Concatenate(cur, parseImage());
            } else {
                cur = new Concatenate(cur, parseSimpleLine(block.equals(")") || block.equals("]")));
            }

            if (!block.isEmpty() && block.charAt(0) != '\n' &&
                    (source.getChar() == Md2HtmlSource.END || source.getChar() == block.charAt(ind))) {
                ind++;
            } else {
                ind = 0;
            }
            if (testNext('\n') && source.getChar() != Md2HtmlSource.END && !test('\r') && source.getSeparators() < 2) {
                cur = new Concatenate(cur, new Const(System.lineSeparator(), source));
            }

            if (ind == 1 && block.length() > 1 && testNext(block.charAt(ind))) {
                break;
            }
        }
        return cur;
    }

    private StringExpression parseImage() throws IOException {
        StringExpression cur;

        if (testNext('[')) {
            cur = parseText(']');
            if (testNext(']')) {
                if (testNext('(')) {
                    StringExpression in = parseText(')');
                    if (testNext(')')) {
                        cur = new Tag(null, "img", in, cur, source);
                    } else {
                        cur = new Concatenate(new Const("[", source), cur);
                        cur = new Concatenate(cur, new Const("](", source));
                        cur = new Concatenate(cur, in);
                    }
                } else {
                    cur = new Concatenate(new Const("[", source), cur);
                    cur = new Concatenate(cur, new Const("]", source));
                }
            } else {
                cur = new Concatenate(new Const("[", source), cur);
            }
        } else {
            cur = new Const("!", source);
        }
        return cur;
    }

    private StringExpression parseLink() throws IOException {
        StringExpression cur = parseLine("]");
        if (testNext(']')) {
            if (testNext('(')) {
                StringExpression in = parseText(')');
                if (testNext(')')) {
                    cur = new Tag(cur, "a", in, source);
                } else {
                    cur = new Concatenate(new Const("[", source), cur);
                    cur = new Concatenate(cur, new Const("](", source));
                    cur = new Concatenate(cur, in);
                }
            } else {
                cur = new Concatenate(new Const("[", source), cur);
                cur = new Concatenate(cur, new Const("]", source));
            }
        } else {
            cur = new Concatenate(new Const("[", source), cur);
        }
        return cur;
    }

    private StringExpression parseLiteral(boolean needTag) {
        String s = symbols.get(source.getChar());
        StringExpression cur = new Const(s, source);
        if (needTag) {
            cur = new Tag(cur, "code", source);
        }
        return cur;
    }

    private StringExpression parseSymbol() {
        if (test('-', '_', '*', '+')) {
            return new Const(Character.toString(source.getChar()), source);
        } else {
            return new Const("\\", source);
        }
    }

    private StringExpression parseHighlight(char type) throws IOException {
        if (type == '`' && (test('<', '>', '&'))) {
            StringExpression cur = parseLiteral(true);
            source.nextChar();
            source.nextChar();
            return cur;
        }
        StringExpression cur = parseLine(Character.toString(type));

        String s = tags.get(type);
        if (testNext(type)) {
            return new Tag(cur, s, source);
        } else {
            return new Concatenate(new Const(Character.toString(type), source), cur);
        }
    }

    private StringExpression parseText(char block) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (source.getChar() != block) {
            sb.append(source.getChar());
            source.nextChar();
        }
        return new Const(sb.toString(), source);
    }


    private StringExpression parseDoubleHighlight() throws IOException {
        char type = source.getChar();
        String d = type + Character.toString(type);

        source.nextChar();
        StringExpression cur = new Const("", source);
        if (testNext(type)) {
            String s = "strong";
            if (type == '-') {
                s = "s";
            }
            if (type == '+') {
                s = "u";
            }
            cur = new Concatenate(cur, new Tag(parseLine(d), s, source));
            source.nextChar();
        } else if (type != '+') {
            cur = new Concatenate(cur, parseHighlight(type));
        }

        return cur;
    }

    private boolean test(char... c) {
        boolean f = false;
        for (char aC : c) {
            if (source.getChar() == aC) {
                f = true;
            }
        }
        return f;
    }

    private boolean notTest(char... chars) {
        for (char c : chars) {
            if (source.getChar() == c) {
                return false;
            }
        }
        return true;
    }

    private boolean testNext(char c) throws IOException {
        if (source.getChar() == c) {
            source.nextChar();
            return true;
        } else {
            return false;
        }
    }
}