package expression.exceptions;

import expression.TripleExpression;
import expression.Const;
import expression.Variable;


public class ExpressionParser implements Parser {

    private ExpressionSource source;

    public TripleExpression parse(String expression) throws NumberException, ParseException {
        source = new StringExpressionSource(expression);
        source.nextChar();
        skip();

        TripleExpression result = plusMinus();
        if (!test(ExpressionSource.END)) {
            expect("end of text");
        }
        return result;
    }

    private void expect(String message) throws ParseException {
        throw source.error("Expected %s, found '%s", message, source.rest());
    }

    private void expect(String message, String add) throws ParseException {
        throw source.error("Expected %s, found '%s", message, add + source.rest());
    }

    private void skip() throws ParseException {
        while (Character.isWhitespace(source.getChar())) {
            source.nextChar();
        }
    }

    private TripleExpression plusMinus() throws NumberException, ParseException {
        TripleExpression result = mulDiv();
        while (true) {
            if (testNext('+')) {
                result = new CheckedAdd(result, mulDiv());
            } else if (testNext('-')) {
                result = new CheckedSubtract(result, mulDiv());
            } else {
                return result;
            }
        }
    }

    private TripleExpression mulDiv() throws NumberException, ParseException {
        TripleExpression result = unary();
        while (true) {
            if (testNext('*')) {
                result = new CheckedMultiply(result, unary());
            } else if (testNext('/')){
                result = new CheckedDivide(result, unary());
            } else {
                return result;
            }
        }
    }

    private TripleExpression unary() throws NumberException, ParseException {
        if (testNext('-')) {
            if (Character.isDigit(source.getChar())) {
                return number("-");
            } else {
                return new CheckedNegate(unary());
            }
        } else if (test('h')) {
            testStringNext("high");
            return new CheckedHigh(unary());
        } else if (test('l')) {
            testStringNext("low");
            return new CheckedLow(unary());
        } else {
            return bracket();
        }
    }

    private TripleExpression bracket() throws NumberException, ParseException {
        if (test(ExpressionSource.END)) {
            expect("value");
        }
        if (testNext('(')) {
            TripleExpression result = plusMinus();
            if (!testNext(')')) {
                expect("closing bracket");
            }
            return result;
        }
        return variable();
    }

    private TripleExpression variable() throws NumberException, ParseException {
        if (test('-') || Character.isDigit(source.getChar())) {
            return number(testNext('-') ? "-" : "");
        }
        if (!test('x') && !test('y') && !test('z')) {
            expect("variable");
        }
        TripleExpression result = new Variable(Character.toString(source.getChar()));
        source.nextChar();
        skip();
        return result;
    }

    private void readDigits(StringBuilder sb) throws ParseException {
        do {
            sb.append(source.getChar());
        } while (Character.isDigit(source.nextChar()));
        skip();
    }

    private TripleExpression number(String sign) throws NumberException, ParseException {
        if (test(ExpressionSource.END)) {
            expect("digits");
        }
        StringBuilder digits = new StringBuilder();
        readDigits(digits);
        try {
            return new Const(Integer.parseInt(sign + digits.toString()));
        } catch (NumberFormatException e) {
            throw new NumberException("Constant is too big: " + (sign + digits.toString()));
        }
    }

    private boolean testNext(char c) throws ParseException {
        if (source.getChar() == c) {
            source.nextChar();
            skip();
            return true;
        }
        return false;
    }

    private void testStringNext(String s) throws ParseException {
        for (int i = 0; i < s.length(); i++) {
            if (source.getChar() != s.charAt(i)) {
                expect(s, s.substring(0, i));
            }
            source.nextChar();
        }
        if (Character.isLetterOrDigit(source.getChar())) {
            expect(s, s);
        }
        skip();
    }

    private boolean test(char c) {
        return source.getChar() == c;
    }

    public static void main(String[] args) throws Exception {
        new ExpressionParser().parse("10");
    }
}
