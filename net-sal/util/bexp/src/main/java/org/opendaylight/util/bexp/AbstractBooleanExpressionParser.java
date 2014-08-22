/*
 * (c) Copyright 2009 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.bexp;

import org.opendaylight.util.SymbolTokenizer;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation of a parser capable of producing a properly structured
 * aggregate boolean expression tree by parsing strings expressions composed
 * of 'not'/'!', 'and'/'&&', 'or'/'||', '(' and ')' symbols.
 * <p>
 * @see <a href="http://forums.sun.com/thread.jspa?threadID=784915">Parsing an Algebraic Expression</a>
 * 
 * @author Thomas Vachuska
 */
public abstract class AbstractBooleanExpressionParser implements
                BooleanExpressionParser {
    
    private static final String[] SYMBOLS = new String[] {
        "not", "and", "or", "!", "&&", "||", "(", ")"
    };

    private static final String SX = " (";
    private static final String EX = ") ";
    
    private static final String[] PRE_DELIMITERS = new String[] {
        SX, EX, EX, SX, EX, EX, null, null
    };

    private static final String[] POST_DELIMITERS = new String[] {
        SX, SX, SX, SX, SX, SX, null, null
    };

    /**
     * Get a boolean expression capable of evaluating the specified leaf
     * token. Derivatives of this class can use this to provide their own leaf
     * token evaluations, while benefiting from the aggregate boolean
     * expression parsing and evaluation.
     * 
     * @param token string token to be used as the leaf boolean expression
     *        definition
     * @return boolean expression for the corresponding token
     */
    public abstract BooleanExpression expression(String token);
    
    /**
     * Get the next token in the specified expression and relative to the
     * given parse position and also appropriately update the give parse
     * position.
     * 
     * @param tokenizer symbol tokenizer to be used for extracting the next
     *        token
     * @return next token, trimmed of all surrounding white-spaces, found in
     *         the expression starting from the given position
     */
    private Token nextToken(SymbolTokenizer tokenizer) {
        if (!tokenizer.hasNext())
            return null;
        
        String word = tokenizer.next();
        boolean wasSymbol = tokenizer.wasSymbol();
        if (wasSymbol && (word.equals("not") || word.equals("!")))
            return NOT_TOKEN;
        else if (wasSymbol && (word.equals("and") || word.equals("&&")))
            return AND_TOKEN;
        else if (wasSymbol && (word.equals("or") || word.equals("||")))
            return OR_TOKEN;
        else if (wasSymbol && word.equals("("))
            return OPEN_TOKEN;
        else if (wasSymbol && word.equals(")"))
            return CLOSE_TOKEN;
        else 
            return new Token(word);
    }

    /**
     * Auxiliary stack to keep pending boolean expressions
     */
    private static class BooleanExpressionStack {
        List<BooleanExpression> stack = new ArrayList<>();
        
        void push(BooleanExpression e) { stack.add(e); }
        BooleanExpression pop() { return stack.remove(stack.size() - 1); }
        int size() { return stack.size(); }
    }
    
    
    @Override
    public BooleanExpression parse(String string) throws ParseException {
        List<Token> list = parseToPostfix(string);
        BooleanExpressionStack stack = new BooleanExpressionStack();
        
        // Now build the boolean expression by scanning the list which is in
        // post-fix order and using a stack of boolean expressions
        for (Token t : list) {
            if (t.type() == ARG)
                stack.push(expression(t.value()));
            else if (t.equals(NOT_TOKEN) && stack.size() > 0)
                stack.push(new Negation(stack.pop()));
            else if (t.equals(AND_TOKEN) && stack.size() > 1)
                stack.push(new Conjunction(stack.pop(), stack.pop()));
            else if (t.equals(OR_TOKEN) && stack.size() > 1)
                stack.push(new Disjunction(stack.pop(), stack.pop()));
            else
                throw new ParseException("Syntax error occurred in" +
                                         " expression " + string + 
                                         "; stack size is " + stack.size() + 
                                         "; suspecting a missing argument", 0);
        }
        
        if (stack.size() != 1)
            throw new ParseException("Syntax error occurred in" +
                                     " expression " + string + 
                                     "; stack size is " + stack.size() + 
                                     "; suspecting a missing keyword", 0);
        return stack.pop();
    }
    
    
    
    /**
     * Parses the given infix expression string into a postfix list of tokens.
     * 
     * @param string string to be parsed
     * @return list of tokens in postfix order
     * @throws ParseException thrown if issues arise while parsing the
     *         expression string
     */
    private List<Token> parseToPostfix(String string) throws ParseException {
        // Setup a stack of tokens for converting infix notation to postfix
        // and a list to hold the postfix sequence of tokens
        Stack stack = new Stack();
        List<Token> out = new ArrayList<>();

        // Start off with a synthetic open
        stack.push(OPEN_TOKEN);

        // Start parsing at the beginning
        SymbolTokenizer tokenizer = new SymbolTokenizer(string, SYMBOLS,
                                                        PRE_DELIMITERS,
                                                        POST_DELIMITERS);
        try {
            int state = 0;
            Token t;
            while ((t = nextToken(tokenizer)) != null) {
                switch (t.type()) {
                case ARG:
                    assertState(string, t, tokenizer.position(), state, 0);
                    out.add(t);
                    state = 1;
                    break;
                    
                case PRE:
                    assertState(string, t, tokenizer.position(), state, 0);
                    stack.push(t);
                    break;
                    
                case OPEN:
                    stack.push(t);
                    break;
                    
                case DI:
                    assertState(string, t, tokenizer.position(), state, 1);
                    stack.pushSpew(out, t);
                    state = 0;
                    break;
                    
                case POST:
                    assertState(string, t, tokenizer.position(), state, 1);
                    stack.pushSpew(out, t);
                    out.add(stack.pop());
                    break;
                    
                case CLOSE:
                default:
                    stack.pushSpew(out, t);
                    stack.removeGroup();
                    break;
                }
            }
            
            // End with a synthetic close
            stack.pushSpew(out, CLOSE_TOKEN);
            stack.removeGroup();
            assertState(string, null, tokenizer.position(), state, 1);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ParseException("Incorrect syntax detected in" +
                                     " expression " + string + 
                                     " at offset " + tokenizer.position(),
                                     tokenizer.position());
        }
        return out;
    }
    
    /**
     * Validates parser state and tosses an exception if state is not valid.
     * 
     * @param string expression string being parsed
     * @param token token where parsing error occurred
     * @param position position where error was encountered
     * @param state encountered symbol/token parser state
     * @param expected expected symbol/token parser state
     * @throws ParseException thrown if the state does not match the expected
     *         state
     */
    private void assertState(String string, Token token, int position,
                             int state, int expected) throws ParseException {
        if (state != expected)
            throw new ParseException("Incorrect syntax detected in" +
                                     " expression " + string +
                                     (token != null ?
                                        " near token " + token.value() : "") +
                                     " at offset " + position, position);
    }

    
    // Type of operations
    private static final int ARG = 0;
    private static final int PRE = 1;
    private static final int POST = 2;
    private static final int DI = 3;
    private static final int OPEN = 4;
    private static final int CLOSE = 5;

    // Priorities of operations are as follows: not, and, or
    static final Token NOT_TOKEN = new Token(PRE, 32, "!");
    static final Token AND_TOKEN = new Token(DI, 16, "&&");
    static final Token OR_TOKEN = new Token(DI, 8, "||");
    
    static final Token CLOSE_TOKEN = new Token(CLOSE, 1, ")");
    static final Token OPEN_TOKEN = new Token(OPEN, 0, "(");
    
    // Token representing a component of expression
    private static class Token {
        private int type;
        private int priority;
        private String value;
        
        Token(String value) { this(ARG, 4, value); }
        
        Token(int type, int priority, String value) {
            this.type = type;
            this.priority = priority;
            this.value  = value;
        }
        
        int type() { return type; }
        int priority() { return priority; }
        String value() { return value; }
    }
    
    // Stack of tokens used to convert the expression from infix to postfix
    private static class Stack {
        private List<Token> list = new ArrayList<>();

        Token peek() { return list.get(list.size() - 1); }
        Token pop() { return list.remove(list.size() - 1); }
        void push(Token t) { list.add(t); }
        void removeGroup() { pop(); pop(); }
        
        void pushSpew(List<Token> out, Token t) {
            while (list.size() > 0 && peek().priority() >= t.priority())
                out.add(pop());
            push(t);
        }
        
    }


}
