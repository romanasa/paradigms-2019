package expression.exceptions;

import expression.TripleExpression;
import expression.Const;
import expression.Variable;

public class ExpressionParser implements Parser {

    public TripleExpression parse(String expressionSpace) throws Exception {
        ExpressionString expression = new ExpressionString(expressionSpace);
        Result result = plusMinus(expression);
        if (!result.rest.isEmpty()) {
            throw new Exception("Can't full parse, expected + or -, found: " + result.rest);
        }
        return result.accumulator;
    }

    private Result plusMinus(ExpressionString expression) throws Exception {
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
                cur.accumulator = new CheckedAdd(accumulator, cur.accumulator);
            } else {
                cur.accumulator = new CheckedSubtract(accumulator, cur.accumulator);
            }
        }
        return cur;
    }

    private Result mulDiv(ExpressionString expression) throws Exception {
        Result cur = unary(expression);
        while (!cur.rest.isEmpty()) {
            if (cur.rest.first() != '*' && cur.rest.first() != '/') {
                break;
            }
            char sign = cur.rest.first();
            cur.rest.removeFirst();

            TripleExpression accumulator = cur.accumulator;
            cur = unary(cur.rest);

            if (sign == '*') {
                cur.accumulator = new CheckedMultiply(accumulator, cur.accumulator);
            } else {
                cur.accumulator = new CheckedDivide(accumulator, cur.accumulator);
            }
        }
        return cur;
    }

    private Result unary(ExpressionString expression) throws Exception {
        Result cur;
        if (expression.hasTwo() && expression.first() == '-' && !Character.isDigit(expression.second())) {
            expression.removeFirst();
            cur = unary(expression);
            cur.accumulator = new CheckedNegate(cur.accumulator);
        } else if (expression.contains("high")) {
            expression.removeFirst(4);
            cur = unary(expression);
            cur.accumulator = new CheckedHigh(cur.accumulator);
        } else if (expression.contains("low")) {
            expression.removeFirst(3);
            cur = unary(expression);
            cur.accumulator = new CheckedLow(cur.accumulator);
        } else {
            cur = bracket(expression);
        }
        return cur;
    }

    private Result bracket(ExpressionString expression) throws Exception {
        if (expression.isEmpty()) {
            throw new Exception("Expected value, found: " + expression);
        }
        if (expression.first() == '(') {
            expression.removeFirst();
            Result cur = plusMinus(expression);
            if (cur.rest.isEmpty() || cur.rest.first() != ')') {
                throw new Exception("Expected close bracket found: " + cur.rest);
            }
            expression.removeFirst();
            return cur;
        }
        return variable(expression);
    }

    private Result variable(ExpressionString expression) throws Exception {
        char name = expression.first();
        if (name == '-' || Character.isDigit(name)) {
            return num(expression);
        }
        if (name != 'x' && name != 'y' && name != 'z') {
            if (Character.isLetter(name)) {
                throw new Exception("Unknown variable: " + expression);
            }
            throw new Exception("Expected value, found: " + expression);
        }
        expression.removeFirst();
        return new Result(new Variable(Character.toString(name)), expression);
    }

    private Result num(ExpressionString expression) throws Exception {
        int sign = 1;
        TripleExpression cur = new Const(0);

        if (expression.first() == '-') {
            sign = -1;
            expression.removeFirst();
        }
        if (expression.isEmpty()) {
            throw new Exception("Expected digits, found: " + expression);
        }
        for (; !expression.isEmpty(); expression.removeFirstWithoutSkip()) {
            if (!Character.isDigit(expression.first())) {
                break;
            }
            int digit = Character.getNumericValue(expression.first()) * sign;
            cur = new CheckedAdd(new CheckedMultiply(cur, new Const(10)), new Const(digit));
        }
        try {
            int val = cur.evaluate(0, 0, 0);
            expression.skip();
            return new Result(new Const(val), expression);
        } catch (Exception e) {
            throw new Exception("Constant too big: " + e.getMessage());
        }
    }
}
