package expression.exceptions;

import expression.TripleExpression;

class Result {
    TripleExpression accumulator;
    ExpressionString rest;

    Result(TripleExpression accumulator, ExpressionString rest) {
        this.accumulator = accumulator;
        this.rest = rest;
    }
}
