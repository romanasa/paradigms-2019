let ids = {
    'x': 0, 'y': 1, 'z': 2
};

let constants = {
    'pi': Math.PI, 'e': Math.E
};

const cnst = c => () => (c);

let pi = cnst(Math.PI);
let e = cnst(Math.E);

let variable = (name) => {
    const id = ids[name];
    return (...args) => args[id];
};



const op = (f) => {
    return (...args) => (...values) => f(...args.map(curf => curf(...values)));
};

let add = op((a, b) => (a + b));
let subtract = op((a, b) => (a - b));
let multiply = op((a, b) => (a * b));
let divide = op((a, b) => (a / b));
let negate = op((a) => (-a));


let med3 = op( function(...args) {
    args.sort((a, b) => a - b);
    let ind = Math.floor(args.length / 2);
    return args[ind];
});

let avg5 = op(function (...args) {
    return args.reduce((a, b) => a + b) / args.length;
});

let parse = function(expression) {
    let st = [];
    let ops = {
        '+': {f: add, len: 2}, '-': {f: subtract, len: 2},
        '*': {f: multiply, len: 2}, '/': {f: divide, len: 2},
        'negate': {f: negate, len: 1},
        'avg5': {f: avg5, len: 5}, 'med3': {f: med3, len: 3}
    };

    for (const elem of expression.split(' ').filter(word => word.length > 0)) {
        if (elem in ops) {
            let curlen = ops[elem].len;
            let cur = st.splice(-curlen, curlen);
            st.push(ops[elem].f(...cur));
        } else if (elem in constants) {
            st.push(cnst(constants[elem]));
        } else {
            if (isNaN(elem)) {
                st.push(variable(elem));
            } else {
                st.push(cnst(Number(elem)));
            }
        }
    }
    return st.pop();
};
