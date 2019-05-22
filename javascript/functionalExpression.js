const ids = {
    'x': 0, 'y': 1, 'z': 2
};

const cnst = c => () => (c);

const constants = {
    pi: cnst(Math.PI), e: cnst(Math.E)
};

const pi = constants.pi;
const e = constants.e;

let variable = (name) => {
    const id = ids[name];
    return (...args) => args[id];
};

const vars = {
    'x': variable('x'), 'y': variable('y'), 'z': variable('z')
};

const op = (f) => (...args) => (...values) => f(...args.map(arg => arg(...values)));

let add = op((a, b) => (a + b));
let subtract = op((a, b) => (a - b));
let multiply = op((a, b) => (a * b));
let divide = op((a, b) => (a / b));
let negate = op(a => -a);

let med3 = op((...args) => args.sort((a, b) => a - b)[Math.floor(args.length / 2)]);
let avg5 = op((...args) => args.reduce((a, b) => a + b) / args.length);

let ops = {
    '+': {f: add, len: 2}, '-': {f: subtract, len: 2},
    '*': {f: multiply, len: 2}, '/': {f: divide, len: 2},
    'negate': {f: negate, len: 1},
    'avg5': {f: avg5, len: 5}, 'med3': {f: med3, len: 3}
};
let parse = function (expression) {
    let st = [];
    for (const elem of expression.split(' ').filter(word => word.length > 0)) {
        if (elem in ops) {
            const {len: len, f: f} = ops[elem];
            let cur = st.splice(-len);
            st.push(f(...cur));
        } else if (elem in constants) {
            st.push(constants[elem]);
        } else if (elem in vars) {
            st.push(vars[elem]);
        } else {
            st.push(cnst(Number(elem)));
        }
    }
    return st.pop();
};
