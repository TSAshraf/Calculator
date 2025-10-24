import java.util.*;
import java.util.regex.*;

public class CalculatorEngine {

    private static class Token {
        enum Type { NUMBER, OP, LPAREN, RPAREN, FUNC, CONST, UNARY }
        Type type;
        String value;
        Token(Type t, String v){ type=t; value=v; }
        public String toString(){ return type+":"+value; }
    }

    private static final Set<String> FUNCTIONS = new HashSet<>(Arrays.asList(
        "sin","cos","tan","sqrt","log","ln"
    ));

    private static final Map<String,Integer> PREC = new HashMap<>();
    private static final Set<String> RIGHT_ASSOC = new HashSet<>();
    static {
        PREC.put("^", 4);
        PREC.put("*", 3);
        PREC.put("/", 3);
        PREC.put("+", 2);
        PREC.put("-", 2);
        // unary minus as highest precedence
        PREC.put("~", 5);
        RIGHT_ASSOC.add("^");
        RIGHT_ASSOC.add("~"); // unary minus
    }

    public String evaluate(String expression) {
        try {
            List<Token> tokens = tokenize(expression);
            List<Token> rpn = shuntingYard(tokens);
            double result = evalRPN(rpn);
            if (Double.isNaN(result) || Double.isInfinite(result)) return "Error";
            // Trim trailing .0 for integers
            String s = Double.toString(result);
            if (s.endsWith(".0")) s = s.substring(0, s.length()-2);
            return s;
        } catch (Exception e) {
            System.err.println("Evaluation error: " + e.getMessage());
            return "Error";
        }
    }

    private List<Token> tokenize(String expr){
        ArrayList<Token> out = new ArrayList<>();
        int i=0, n=expr.length();
        Token prev = null;
        while(i<n){
            char c = expr.charAt(i);
            if (Character.isWhitespace(c)) { i++; continue; }
            if (Character.isDigit(c) || c=='.' || (c=='-' && (prev==null || prev.type==Token.Type.OP || prev.type==Token.Type.LPAREN || prev.type==Token.Type.UNARY) && i+1<n && (Character.isDigit(expr.charAt(i+1)) || expr.charAt(i+1)=='.'))) {
                // number (possibly negative)
                int j=i+1;
                while(j<n && (Character.isDigit(expr.charAt(j)) || expr.charAt(j)=='.')) j++;
                String num = expr.substring(i,j);
                out.add(new Token(Token.Type.NUMBER, num));
                prev = out.get(out.size()-1);
                i=j;
            } else if (c=='('){
                out.add(new Token(Token.Type.LPAREN,"("));
                prev = out.get(out.size()-1);
                i++;
            } else if (c==')'){
                out.add(new Token(Token.Type.RPAREN,")"));
                prev = out.get(out.size()-1);
                i++;
            } else if ("+-*/^".indexOf(c)>=0){
                String op = String.valueOf(c);
                // handle unary minus when not followed by a digit (e.g., -(3+4), -sin(30))
                if (op.equals("-") && (prev==null || prev.type==Token.Type.OP || prev.type==Token.Type.LPAREN || prev.type==Token.Type.UNARY)){
                    out.add(new Token(Token.Type.UNARY,"~"));
                } else {
                    out.add(new Token(Token.Type.OP, op));
                }
                prev = out.get(out.size()-1);
                i++;
            } else if (c=='π' || c=='\u03C0'){
                out.add(new Token(Token.Type.CONST,"pi"));
                prev = out.get(out.size()-1);
                i++;
            } else if (Character.isLetter(c)){
                int j=i+1;
                while(j<n && Character.isLetter(expr.charAt(j))) j++;
                String name = expr.substring(i,j).toLowerCase();
                if (name.equals("pi")) {
                    out.add(new Token(Token.Type.CONST,"pi"));
                } else if (FUNCTIONS.contains(name)){
                    out.add(new Token(Token.Type.FUNC, name));
                } else {
                    throw new RuntimeException("Unknown identifier: "+name);
                }
                prev = out.get(out.size()-1);
                i=j;
            } else {
                throw new RuntimeException("Unexpected char: "+c);
            }
        }
        return out;
    }

    private List<Token> shuntingYard(List<Token> tokens){
        ArrayList<Token> output = new ArrayList<>();
        Deque<Token> stack = new ArrayDeque<>();
        for (Token t : tokens){
            switch(t.type){
                case NUMBER:
                case CONST:
                    output.add(t);
                    break;
                case FUNC:
                    stack.push(t);
                    break;
                case OP:
                case UNARY:
                    String op1 = t.value;
                    while(!stack.isEmpty() && (stack.peek().type==Token.Type.OP || stack.peek().type==Token.Type.UNARY)){
                        String op2 = stack.peek().value;
                        int p1 = PREC.get(op1);
                        int p2 = PREC.get(op2);
                        if ((RIGHT_ASSOC.contains(op1) && p1 < p2) || (!RIGHT_ASSOC.contains(op1) && p1 <= p2)){
                            output.add(stack.pop());
                        } else break;
                    }
                    stack.push(t);
                    break;
                case LPAREN:
                    stack.push(t);
                    break;
                case RPAREN:
                    while(!stack.isEmpty() && stack.peek().type!=Token.Type.LPAREN){
                        output.add(stack.pop());
                    }
                    if (stack.isEmpty()) throw new RuntimeException("Mismatched parentheses");
                    stack.pop(); // remove '('
                    if (!stack.isEmpty() && stack.peek().type==Token.Type.FUNC){
                        output.add(stack.pop());
                    }
                    break;
            }
        }
        while(!stack.isEmpty()){
            Token t2 = stack.pop();
            if (t2.type==Token.Type.LPAREN || t2.type==Token.Type.RPAREN) throw new RuntimeException("Mismatched parentheses");
            output.add(t2);
        }
        return output;
    }

    private double evalRPN(List<Token> rpn){
        Deque<Double> st = new ArrayDeque<>();
        for (Token t : rpn){
            switch(t.type){
                case NUMBER:
                    st.push(Double.parseDouble(t.value));
                    break;
                case CONST:
                    if (t.value.equals("pi")) st.push(Math.PI);
                    else throw new RuntimeException("Unknown const");
                    break;
                case OP: {
                    if (st.size()<2) throw new RuntimeException("Insufficient values");
                    double b = st.pop();
                    double a = st.pop();
                    switch(t.value){
                        case "+": st.push(a+b); break;
                        case "-": st.push(a-b); break;
                        case "*": st.push(a*b); break;
                        case "/": st.push(a/b); break;
                        case "^": st.push(Math.pow(a,b)); break;
                        default: throw new RuntimeException("Unknown op");
                    }
                    break;
                }
                case UNARY: {
                    if (st.isEmpty()) throw new RuntimeException("Insufficient values");
                    double a = st.pop();
                    st.push(-a);
                    break;
                }
                case FUNC: {
                    if (st.isEmpty()) throw new RuntimeException("Insufficient values");
                    double a = st.pop();
                    switch(t.value){
                        case "sin": st.push(Math.sin(a)); break;
                        case "cos": st.push(Math.cos(a)); break;
                        case "tan": st.push(Math.tan(a)); break;
                        case "sqrt": st.push(Math.sqrt(a)); break;
                        case "log": st.push(Math.log10(a)); break;
                        case "ln": st.push(Math.log(a)); break;
                        default: throw new RuntimeException("Unknown func");
                    }
                    break;
                }
                default:
                    throw new RuntimeException("Bad token in RPN: "+t);
            }
        }
        if (st.size()!=1) throw new RuntimeException("Too many values");
        return st.pop();
    }
}