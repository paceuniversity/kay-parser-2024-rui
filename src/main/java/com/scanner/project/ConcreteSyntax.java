package com.scanner.project;

public class ConcreteSyntax {

    private final TokenStream input;
    private Token current;

    public ConcreteSyntax(TokenStream ts) {
        this.input = ts;
        this.current = input.nextToken();
    }

    private void next() {
        current = input.nextToken();
    }

    private void error(String expected) {
        throw new RuntimeException(
                "Syntax error - Expecting: " + expected +
                " But saw: " + current.getType() + " = " + current.getValue());
    }

    public Program program() {
        Program p = new Program();

        if (!"Keyword".equals(current.getType()) ||
            !"main".equals(current.getValue())) {
            error("main");
        }
        next();

        if (!"Separator".equals(current.getType()) ||
            !"{".equals(current.getValue())) {
            error("{");
        }
        next();

        p.decpart = declarations();

        Block body = new Block();
        statements(body);
        if (!"Separator".equals(current.getType()) ||
            !"}".equals(current.getValue())) {
            error("}");
        }
        next();
        p.body = body;
        return p;
    }

    private Declarations declarations() {
        Declarations decs = new Declarations();
        while ("Keyword".equals(current.getType()) &&
              ("integer".equals(current.getValue()) ||
               "bool".equals(current.getValue()))) {

            String typeName = current.getValue();
            Type t = new Type(typeName);
            next();

            if (!"Identifier".equals(current.getType())) {
                error("Identifier");
            }
            while (true) {
                Declaration d = new Declaration();
                Variable v = new Variable();
                v.id = current.getValue();
                d.v = v;
                d.t = t;
                decs.add(d);
                next();
                if ("Separator".equals(current.getType()) &&
                    ",".equals(current.getValue())) {
                    next();
                    if (!"Identifier".equals(current.getType())) {
                        error("Identifier");
                    }
                } else {
                    break;
                }
            }

            if (!"Separator".equals(current.getType()) ||
                !";".equals(current.getValue())) {
                error(";");
            }
            next();
        }
        return decs;
    }

    private void statements(Block b) {
        while (!("Separator".equals(current.getType()) &&
                 "}".equals(current.getValue()))) {
            Statement s = statement();
            if (s != null) {
                b.blockmembers.add(s);
            }
        }
    }

    private Statement statement() {
        if ("Keyword".equals(current.getType()) &&
            "if".equals(current.getValue())) {
            return ifStatement();
        } else if ("Keyword".equals(current.getType()) &&
                   "while".equals(current.getValue())) {
            return whileStatement();
        } else if ("Identifier".equals(current.getType())) {
            return assignment();
        } else {
            error("Statement");
            return null;
        }
    }

    private Assignment assignment() {
        Assignment a = new Assignment();
        Variable v = new Variable();
        v.id = current.getValue();
        a.target = v;
        next();

        if (!"Operator".equals(current.getType()) ||
            !":=".equals(current.getValue())) {
            error(":=");
        }
        next();

        a.source = expression();

        if (!"Separator".equals(current.getType()) ||
            !";".equals(current.getValue())) {
            error(";");
        }
        next();

        return a;
    }

    private Conditional ifStatement() {
        Conditional c = new Conditional();
        next();
        if (!"Separator".equals(current.getType()) ||
            !"(".equals(current.getValue())) {
            error("(");
        }
        next();

        c.test = expression();

        if (!"Separator".equals(current.getType()) ||
            !")".equals(current.getValue())) {
            error(")");
        }
        next();

        c.thenbranch = blockWithoutDecls();

        if ("Keyword".equals(current.getType()) &&
            "else".equals(current.getValue())) {
            next();
            c.elsebranch = blockWithoutDecls();
        } else {
            c.elsebranch = null;
        }
        return c;
    }

    private Loop whileStatement() {
        Loop l = new Loop();
        next();

        if (!"Separator".equals(current.getType()) ||
            !"(".equals(current.getValue())) {
            error("(");
        }
        next();

        l.test = expression();

        if (!"Separator".equals(current.getType()) ||
            !")".equals(current.getValue())) {
            error(")");
        }
        next();

        l.body = blockWithoutDecls();
        return l;
    }

    private Block blockWithoutDecls() {
        if (!"Separator".equals(current.getType()) ||
            !"{".equals(current.getValue())) {
            error("{");
        }
        next();
        Block b = new Block();
        statements(b);
        if (!"Separator".equals(current.getType()) ||
            !"}".equals(current.getValue())) {
            error("}");
        }
        next();
        return b;
    }

    private Expression expression() {
        return orExpr();
    }

    private Expression orExpr() {
        Expression left = andExpr();
        while ("Operator".equals(current.getType()) &&
               "||".equals(current.getValue())) {
            Operator op = new Operator("||");
            next();
            Expression right = andExpr();
            Binary b = new Binary();
            b.op = op;
            b.term1 = left;
            b.term2 = right;
            left = b;
        }
        return left;
    }

    private Expression andExpr() {
        Expression left = relExpr();
        while ("Operator".equals(current.getType()) &&
               "&&".equals(current.getValue())) {
            Operator op = new Operator("&&");
            next();
            Expression right = relExpr();
            Binary b = new Binary();
            b.op = op;
            b.term1 = left;
            b.term2 = right;
            left = b;
        }
        return left;
    }

    private Expression relExpr() {
        Expression left = addExpr();
        if ("Operator".equals(current.getType()) &&
            ( "<".equals(current.getValue())  ||
              "<=".equals(current.getValue()) ||
              ">".equals(current.getValue())  ||
              ">=".equals(current.getValue()) ||
              "==".equals(current.getValue()) ||
              "!=".equals(current.getValue()) )) {

            String opLex = current.getValue();
            if ("!=".equals(opLex)) {
                opLex = "<>";
            }
            Operator op = new Operator(opLex);
            next();
            Expression right = addExpr();
            Binary b = new Binary();
            b.op = op;
            b.term1 = left;
            b.term2 = right;
            left = b;
        }
        return left;
    }

    private Expression addExpr() {
        Expression left = mulExpr();
        while ("Operator".equals(current.getType()) &&
               ("+".equals(current.getValue()) ||
                "-".equals(current.getValue()))) {
            Operator op = new Operator(current.getValue());
            next();
            Expression right = mulExpr();
            Binary b = new Binary();
            b.op = op;
            b.term1 = left;
            b.term2 = right;
            left = b;
        }
        return left;
    }

    private Expression mulExpr() {
        Expression left = unary();
        while ("Operator".equals(current.getType()) &&
               ("*".equals(current.getValue()) ||
                "/".equals(current.getValue()))) {
            Operator op = new Operator(current.getValue());
            next();
            Expression right = unary();
            Binary b = new Binary();
            b.op = op;
            b.term1 = left;
            b.term2 = right;
            left = b;
        }
        return left;
    }

    private Expression unary() {
        if ("Operator".equals(current.getType()) &&
            ("!".equals(current.getValue()) ||
             "-".equals(current.getValue()))) {
            Operator op = new Operator(current.getValue());
            next();
            Expression term = unary();
            Unary u = new Unary();
            u.op = op;
            u.term = term;
            return u;
        }
        return primary();
    }

    private Expression primary() {
        if ("Identifier".equals(current.getType())) {
            Variable v = new Variable();
            v.id = current.getValue();
            next();
            return v;
        } else if ("Literal".equals(current.getType())) {
            String val = current.getValue();
            Value v;
            if ("True".equals(val) || "true".equals(val)) {
                v = new Value(true);
            } else if ("False".equals(val) || "false".equals(val)) {
                v = new Value(false);
            } else {
                v = new Value(Integer.parseInt(val));
            }
            next();
            return v;
        } else if ("Separator".equals(current.getType()) &&
                   "(".equals(current.getValue())) {
            next();
            Expression e = expression();
            if (!"Separator".equals(current.getType()) ||
                !")".equals(current.getValue())) {
                error(")");
            }
            next();
            return e;
        } else {
            error("Expression");
            return new Value();
        }
    }
}
