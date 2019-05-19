"use strict";
let ids = {
    "x": 0, "y": 1, "z": 2
};

function Const(value) {
    this.value = value;
}

const ONE = new Const(1);
const ZERO = new Const(0);

Const.prototype.evaluate = function () {
    return this.value;
};
Const.prototype.toString = Const.prototype.prefix = Const.prototype.postfix = function () {
    return this.value.toString();
};
Const.prototype.diff = function () {
    return ZERO;
};

function Variable(name) {
    this.name = name;
}

Variable.prototype.evaluate = function (...args) {
    return args[ids[this.name]];
};
Variable.prototype.toString = Variable.prototype.prefix = Variable.prototype.postfix = function () {
    return this.name;
};
Variable.prototype.diff = function (name) {
    return this.name === name ? ONE : ZERO;
};

const vars = {
    'x': new Variable('x'), 'y': new Variable('y'), 'z': new Variable('z')
};

function Operation(f, name, diffObject, ...objects) {
    this.f = f;
    this.objects = objects;
    this.name = name;
    this.diffObject = diffObject;
}

Operation.prototype.evaluate = function (...args) {
    return this.f(...this.objects.map(elem => elem.evaluate(...args)));
};
Operation.prototype.toString = function () {
    return this.objects.join(" ") + " " + this.name;
};
Operation.prototype.prefix = function () {
    return "(" + this.name + " " + this.objects.map(elem => elem.prefix()).join(" ") + ")";
};
Operation.prototype.postfix = function () {
    return "(" + this.objects.map(elem => elem.postfix()).join(" ") + " " + this.name + ")";
};
Operation.prototype.diff = function (name) {
    return this.diffObject(...this.objects, ...this.objects.map(elem => elem.diff(name)));
};

function newOperation(op, name, diffOp) {
    return function (...args) {
        return new Operation(op, name, diffOp, ...args);
    };
}

const Add = newOperation((a, b) => a + b, "+", (a, b, da, db) => new Add(da, db));
const Subtract = newOperation((a, b) => a - b, "-", (a, b, da, db) => new Subtract(da, db));
const Multiply = newOperation((a, b) => a * b, "*", (a, b, da, db) =>
    new Add(new Multiply(a, db), new Multiply(b, da)));

const Divide = newOperation((a, b) => a / b, "/", (a, b, da, db) =>
    new Divide(new Subtract(new Multiply(da, b), new Multiply(db, a)), new Multiply(b, b)));

const Negate = newOperation(x => -x, "negate", (a, da) => new Negate(da));

const ArcTan = newOperation(Math.atan, "atan", (a, da) =>
    new Multiply(da, new Divide(ONE, new Add(ONE, new Multiply(a, a)))));

const ArcTan2 = newOperation(Math.atan2, "atan2", (a, b, da, db) =>
    new Divide(
        new Subtract(new Multiply(b, da), new Multiply(a, db)),
        new Add(new Multiply(a, a), new Multiply(b, b))));


function sumexpDerivative(...args) {
    let cur = ZERO;
    for (let i = 0; i < args.length / 2; i++) {
        cur = new Add(cur, new Multiply(new Sumexp(args[i]),
            args[i + args.length / 2]));
    }
    return cur;
}

const Sumexp = newOperation((...args) => args.reduce((a, b) => (a + Math.exp(b)), 0), "sumexp",
    sumexpDerivative);


const Softmax = newOperation(function (...args) {
        return Math.exp(args[0]) / args.reduce((a, b) => (a + Math.exp(b)), 0);
    }, "softmax",
    function (...args) {
        let a = new Sumexp(args[0]);
        let cur = sumexpDerivative(...args);
        let exp = new Sumexp(...args.slice(0, args.length / 2));
        return new Divide(new Subtract(new Multiply(new Multiply(a, args[args.length / 2]), exp),
            new Multiply(cur, a)), new Multiply(exp, exp));
    });

const ops = {
    "+": {Op: Add, len: 2}, "-": {Op: Subtract, len: 2},
    "*": {Op: Multiply, len: 2}, "/": {Op: Divide, len: 2},
    "negate": {Op: Negate, len: 1}, "atan": {Op: ArcTan, len: 1},
    "atan2": {Op: ArcTan2, len: 2}, "sumexp": {Op: Sumexp, len: 0},
    "softmax": {Op: Softmax, len: 0}
};

let parse = function (expression) {
    let st = [];
    for (const elem of expression.split(" ").filter(word => word.length > 0)) {
        if (elem in ops) {
            let curLen = ops[elem].len;
            let cur = st.splice(-curLen, curLen);
            st.push(new ops[elem].Op(...cur));
        } else if (elem in vars) {
            st.push(vars[elem]);
        } else {
            st.push(new Const(Number(elem)));
        }
    }
    return st.pop();
};

function ParseException(message) {
    this.message = message;
    this.name = "ParseException";
}
ParseException.prototype = Error.prototype;

const parser = (mode) => {
    let source;
    let pos = 0;
    const END = String.fromCharCode(0);

    const Modes = {
        "prefix": prefix,
        "postfix": postfix
    };

    function isDigit(c) {
        return c !== " " && !isNaN(c);
    }

    function isWhiteSpace(c) {
        return /\s/.test(c);
    }

    function isAlpha(c) {
        return /[A-Za-z]/.test(c);
    }

    function isAlphaNumeric(c) {
        return isDigit(c) || isAlpha(c);
    }

    function skip() {
        while (isWhiteSpace(read())) {
            pos++;
        }
    }

    function skipNumber(number) {
        pos += number;
        skip();
    }

    function read() {
        return source.charAt(pos);
    }

    function readNext() {
        let res = read();
        pos++;
        return res;
    }

    function next() {
        pos++;
        skip();
    }

    function nextCharacter() {
        return test(END) ? END : source.charAt(pos + 1);
    }

    function test(c) {
        return source.charAt(pos) === c;
    }

    function testNext(c) {
        let res = test(c);
        if (res) {
            next();
        }
        return res;
    }

    function checkOperator(op) {
        return testString(op) && !(op === "-" && isDigit(nextCharacter()));
    }

    function testOperator() {
        return Object.keys(ops).some(checkOperator);
    }

    function testString(s) {
        return source.substr(pos, s.length) === s && !isAlphaNumeric(source[pos + s.length]);
    }

    function expect(expected) {
        throw new ParseException(pos + ": Expected " + expected + ", found: \"" +
            (test(END) ? "end of source" : source.substr(pos)) + "\"" + "\n" + source + "\n" +
            "-".repeat(pos) + "^" + "-".repeat(source.length - pos - 1));
    }

    function readTokens(cnt = 0) {
        let args = [];
        for (let i = 0; i < cnt || cnt === 0; i++) {
            if (test(")") || (mode === "postfix" && testOperator())) {
                break;
            }
            if (testOperator()) {
                expect("token");
            }
            args.push(unary());
        }
        return args;
    }

    function prefix() {
        let op = operator();
        let cnt = ops[op].len;
        let args = readTokens(cnt);
        return [op, cnt, args];
    }

    function postfix() {
        let args = readTokens();
        let op = operator();
        let cnt = ops[op].len;
        return [op, cnt, args];
    }

    function token() {
        if (!testNext("(")) {
            expect("opening bracket");
        }
        let cur = Modes[mode]();
        let [operator, cnt, args] = cur;

        if (args.length !== cnt && cnt > 0) {
            expect("arguments");
        }

        if (!testNext(")")) {
            expect("closing bracket");
        }
        return new ops[operator].Op(...args);
    }

    function operator() {
        for (const op in ops) {
            if (checkOperator(op)) {
                skipNumber(op.length);
                return op;
            }
        }
        expect("operator");
    }

    function unary() {
        let cur;
        if (testString("negate")) {
            skipNumber(6);
            cur = unary();
            cur = new Negate(cur);
        } else if (testString("atan")) {
            skipNumber(4);
            cur = unary();
            cur = new ArcTan(cur);
        } else {
            cur = bracket();
        }
        return cur;
    }

    function bracket() {
        if (test(END)) {
            expect("value");
        }
        if (test("(")) {
            return token();
        }
        return variable();
    }

    function variable() {
        if (test("-") || isDigit(read())) {
            return num();
        }
        if (test("x") || test("y") || test("z")) {
            let name = readNext();
            if (isAlpha(read())) {
                throw new ParseException("Unknown variable: " + source.substr(pos));
            }
            skip();
            return new Variable(name);
        }
        if (isAlpha(read())) {
            throw new ParseException("Unknown variable: " + source.substr(pos));
        }
        expect("value");
    }

    function readDigits() {
        let cur = "";
        while (isDigit(read())) {
            cur += read();
            pos++;
        }
        skip();
        return cur;
    }

    function num() {
        let sign = 1;
        if (testNext("-")) {
            sign = -1;
        }
        let curd = readDigits();
        return new Const(Number.parseInt(curd) * sign);
    }

    function parse(expression) {
        pos = 0;
        source = expression + END;
        skip();

        let cur;
        try {
            cur = unary(true);
            if (test(END)) {
                return cur;
            }
        } catch (e) {
        }

        pos = 0;
        cur = token();
        if (!test(END)) {
            expect("end of text");
        }
        return cur;
    }

    return parse;
};

const parsePrefix = parser("prefix");
const parsePostfix = parser("postfix");
