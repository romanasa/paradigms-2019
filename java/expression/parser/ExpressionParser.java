package expression.parser;

import expression.*;

public class ExpressionParser implements Parser {

    public TripleExpression parse(String expressionSpace) {
        StringBuilder expression = new StringBuilder();
        for (int i = 0; i < expressionSpace.length(); i++) {
            char c = expressionSpace.charAt(i);
            if (!Character.isWhitespace(c)) {
                expression.append(c);
            }
        }

        Result result = plusMinus(new ExpressionString(expression.toString()));
        if (!result.rest.isEmpty()) {
            System.err.println("Can't full parse:");
            System.err.println("rest: " + result.rest);
            System.exit(0);
        }
        return result.accumulator;
    }

    private Result plusMinus(ExpressionString expression) {
        Result cur = mulDiv(expression);
        while (!cur.rest.isEmpty()) {
            if (cur.rest.first() != '+' && cur.rest.first() != '-') {
                break;
            }
            char sign = cur.rest.first();
            cur.rest.removeFirst();

            TripleExpression accumulator = cur.accumulator;
            cur = mulDiv(cur.rest);

            if (sign == '+') {
                cur.accumulator = new Add(accumulator, cur.accumulator);
            } else {
                cur.accumulator = new Subtract(accumulator, cur.accumulator);
            }
        }
        return cur;
    }

    private Result mulDiv(ExpressionString expression) {
        Result cur = neg(expression);
        while (!cur.rest.isEmpty()) {
            if (cur.rest.first() != '*' && cur.rest.first() != '/') {
                break;
            }
            char sign = cur.rest.first();
            cur.rest.removeFirst();

            TripleExpression accumulator = cur.accumulator;
            cur = neg(cur.rest);

            if (sign == '*') {
                cur.accumulator = new Multiply(accumulator, cur.accumulator);
            } else {
                cur.accumulator = new Divide(accumulator, cur.accumulator);
            }
        }
        return cur;
    }

    private Result neg(ExpressionString expression) {
        char sign = '+';
        while (!expression.isEmpty() && expression.first() == '-') {
            sign = (char)((int)'+' ^ (int)'-' ^ sign);
            expression.removeFirst();
        }
        Result cur = bracket(expression);
        if (sign == '-') {
            cur.accumulator = new Multiply(new Const(-1), cur.accumulator);
        }
        return cur;
    }

    private Result bracket(ExpressionString expression) {
        if (expression.first() == '(') {
            expression.removeFirst();
            Result cur = plusMinus(expression);
            if (cur.rest.isEmpty() || cur.rest.first() != ')') {
                System.err.println("Not close bracket " + cur.rest);
                System.exit(0);
            }
            expression.removeFirst();
            return cur;
        }
        return variable(expression);
    }

    private Result variable(ExpressionString expression) {
        char name = expression.first();
        if (name == '-' || Character.isDigit(name)) {
            return num(expression);
        }
        if (name != 'x' && name != 'y' && name != 'z') {
            System.err.println("Unknown variable: " + expression);
            System.exit(0);
        }
        expression.removeFirst();
        return new Result(new Variable(Character.toString(name)), expression);
    }

    private Result num(ExpressionString expression) {
        int sign = 1;
        int cur = 0;

        if (expression.first() == '-') {
            sign = -1;
            expression.removeFirst();
        }
        for (; !expression.isEmpty(); expression.removeFirst()) {
            if (!Character.isDigit(expression.first())) {
                break;
            }
            cur = cur * 10 + (expression.first() - '0');
        }
        return new Result(new Const(sign * cur), expression);
    }
}
