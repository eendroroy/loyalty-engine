package io.github.eendroroy.loyalty.rule;

import io.github.eendroroy.loyalty.enums.RewardType;
import io.github.eendroroy.loyalty.rule.ast.AndNode;
import io.github.eendroroy.loyalty.rule.ast.ComparisonOperator;
import io.github.eendroroy.loyalty.rule.ast.Condition;
import io.github.eendroroy.loyalty.rule.ast.FieldRef;
import io.github.eendroroy.loyalty.rule.ast.LeafNode;
import io.github.eendroroy.loyalty.rule.ast.LogicalNode;
import io.github.eendroroy.loyalty.rule.ast.OrNode;
import io.github.eendroroy.loyalty.rule.ast.ParsedRule;
import io.github.eendroroy.loyalty.rule.ast.RewardSpec;
import io.github.eendroroy.loyalty.rule.exception.RuleParseException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the loyalty rule DSL into an AST ({@link ParsedRule}).
 *
 * <h3>Grammar</h3>
 * <pre>
 * rule_expr      = "WHEN" logical_expr "THEN" reward
 * logical_expr   = logical_term  ( "OR"  logical_term  )*
 * logical_term   = logical_factor ("AND" logical_factor)*
 * logical_factor = "(" logical_expr ")" | condition
 * condition      = field_ref operator value
 * field_ref      = IDENTIFIER "." IDENTIFIER
 * operator       = ">" | "<" | ">=" | "<=" | "=" | "!=" | "CONTAINS" | "STARTS_WITH" | "ENDS_WITH"
 * value          = NUMBER | DATE | STRING | BOOLEAN | IDENTIFIER
 * reward         = "Point" "(" NUMBER ")" | "Voucher" "(" IDENTIFIER ")"
 * </pre>
 *
 * <h3>Examples</h3>
 * <pre>
 * WHEN transaction.amount > 50 THEN Point(100)
 * WHEN transaction.amount > 20 AND transaction.category = "grocery" THEN Point(30)
 * WHEN (transaction.amount > 100 OR transaction.tier = "gold") AND transaction.active = true
 *      THEN Voucher(SUMMER25)
 * </pre>
 */
@Component
public class RuleParser {

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Parses the given expression string.
     *
     * @param expression the {@code WHEN … THEN …} rule expression
     * @return a fully validated {@link ParsedRule}
     * @throws RuleParseException if the expression contains syntax errors
     */
    public ParsedRule parse(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new RuleParseException("Rule expression must not be empty");
        }
        var tokens = new Lexer(expression.trim()).tokenize();
        var pos    = new int[]{0};

        expect(tokens, pos, TT.WHEN,  "keyword WHEN");
        var condition = parseLogical(tokens, pos);
        expect(tokens, pos, TT.THEN,  "keyword THEN");
        var reward    = parseReward(tokens, pos);

        if (peek(tokens, pos).type() != TT.EOF) {
            throw new RuleParseException("Unexpected token after reward: '" + peek(tokens, pos).value() + "'");
        }
        return new ParsedRule(condition, reward);
    }

    // ── Recursive descent ─────────────────────────────────────────────────────

    /** logical_expr = logical_term ("OR" logical_term)* */
    private LogicalNode parseLogical(List<Token> tokens, int[] pos) {
        var left = parseTerm(tokens, pos);
        while (peek(tokens, pos).type() == TT.OR) {
            consume(tokens, pos);
            var right = parseTerm(tokens, pos);
            left = new OrNode(left, right);
        }
        return left;
    }

    /** logical_term = logical_factor ("AND" logical_factor)* */
    private LogicalNode parseTerm(List<Token> tokens, int[] pos) {
        var left = parseFactor(tokens, pos);
        while (peek(tokens, pos).type() == TT.AND) {
            consume(tokens, pos);
            var right = parseFactor(tokens, pos);
            left = new AndNode(left, right);
        }
        return left;
    }

    /** logical_factor = "(" logical_expr ")" | condition */
    private LogicalNode parseFactor(List<Token> tokens, int[] pos) {
        if (peek(tokens, pos).type() == TT.LPAREN) {
            consume(tokens, pos);
            var node = parseLogical(tokens, pos);
            expect(tokens, pos, TT.RPAREN, "closing parenthesis ')'");
            return node;
        }
        return parseCondition(tokens, pos);
    }

    /** condition = field_ref operator value */
    private LeafNode parseCondition(List<Token> tokens, int[] pos) {
        var fieldRefToken = expect(tokens, pos, TT.FIELD_REF, "field reference (e.g. source.fieldName)");
        var parts         = fieldRefToken.value().split("\\.", 2);
        var fieldRef      = new FieldRef(parts[0], parts[1]);
        var operator      = parseOperator(tokens, pos);
        var rawValue      = parseValue(tokens, pos);
        return new LeafNode(new Condition(fieldRef, operator, rawValue));
    }

    private ComparisonOperator parseOperator(List<Token> tokens, int[] pos) {
        var token = consume(tokens, pos);
        return switch (token.type()) {
            case GT          -> ComparisonOperator.GT;
            case LT          -> ComparisonOperator.LT;
            case GTE         -> ComparisonOperator.GTE;
            case LTE         -> ComparisonOperator.LTE;
            case EQ          -> ComparisonOperator.EQ;
            case NEQ         -> ComparisonOperator.NEQ;
            case CONTAINS    -> ComparisonOperator.CONTAINS;
            case STARTS_WITH -> ComparisonOperator.STARTS_WITH;
            case ENDS_WITH   -> ComparisonOperator.ENDS_WITH;
            default -> throw new RuleParseException(
                    "Expected an operator (>, <, >=, <=, =, !=, CONTAINS, STARTS_WITH, ENDS_WITH) but got '"
                    + token.value() + "'");
        };
    }

    private String parseValue(List<Token> tokens, int[] pos) {
        var token = consume(tokens, pos);
        return switch (token.type()) {
            case NUMBER_VAL, DATE_VAL, STRING_VAL, BOOL_VAL, WORD -> token.value();
            default -> throw new RuleParseException(
                    "Expected a value (number, date, string, boolean, or identifier) but got '"
                    + token.value() + "'");
        };
    }

    /** reward = "Point" "(" NUMBER ")" | "Voucher" "(" (WORD | STRING_VAL) ")" */
    private RewardSpec parseReward(List<Token> tokens, int[] pos) {
        var token = consume(tokens, pos);
        if (token.type() != TT.WORD) {
            throw new RuleParseException(
                    "Expected a reward function ('Point' or 'Voucher') but got '" + token.value() + "'");
        }
        return switch (token.value().toLowerCase()) {
            case "point" -> {
                expect(tokens, pos, TT.LPAREN, "'(' after Point");
                var amount = expect(tokens, pos, TT.NUMBER_VAL, "point amount (e.g. 30)");
                expect(tokens, pos, TT.RPAREN, "')' after point amount");
                yield new RewardSpec(RewardType.POINT, amount.value());
            }
            case "voucher" -> {
                expect(tokens, pos, TT.LPAREN, "'(' after Voucher");
                var code = consume(tokens, pos);
                if (code.type() != TT.WORD && code.type() != TT.STRING_VAL) {
                    throw new RuleParseException(
                            "Expected a voucher code (e.g. SUMMER25) but got '" + code.value() + "'");
                }
                expect(tokens, pos, TT.RPAREN, "')' after voucher code");
                yield new RewardSpec(RewardType.VOUCHER, code.value());
            }
            default -> throw new RuleParseException(
                    "Unknown reward type '" + token.value() + "'. Use 'Point(<amount>)' or 'Voucher(<code>)'");
        };
    }

    // ── Token helpers ─────────────────────────────────────────────────────────

    private Token peek(List<Token> tokens, int[] pos) {
        return pos[0] < tokens.size() ? tokens.get(pos[0]) : new Token(TT.EOF, "<end>");
    }

    private Token consume(List<Token> tokens, int[] pos) {
        return pos[0] < tokens.size() ? tokens.get(pos[0]++) : new Token(TT.EOF, "<end>");
    }

    private Token expect(List<Token> tokens, int[] pos, TT type, String expected) {
        var token = consume(tokens, pos);
        if (token.type() != type) {
            throw new RuleParseException("Expected " + expected + " but got '" + token.value() + "'");
        }
        return token;
    }

    // ── Token types ───────────────────────────────────────────────────────────

    private enum TT {
        WHEN, THEN, AND, OR,
        LPAREN, RPAREN,
        FIELD_REF,
        GT, LT, GTE, LTE, EQ, NEQ,
        CONTAINS, STARTS_WITH, ENDS_WITH,
        NUMBER_VAL, DATE_VAL, STRING_VAL, BOOL_VAL,
        WORD,
        EOF
    }

    private record Token(TT type, String value) {}

    // ── Lexer ─────────────────────────────────────────────────────────────────

    private static final class Lexer {

        private final String input;
        private int pos;

        Lexer(String input) {
            this.input = input;
            this.pos   = 0;
        }

        List<Token> tokenize() {
            var tokens = new ArrayList<Token>();
            while (pos < input.length()) {
                skipWhitespace();
                if (pos >= input.length()) break;

                char c = input.charAt(pos);

                if (c == '"' || c == '\'') {
                    tokens.add(readString(c));
                } else if (c == '-' && pos + 1 < input.length() && Character.isDigit(input.charAt(pos + 1))) {
                    // Negative number
                    pos++;
                    var num = readNumberOrDate();
                    tokens.add(new Token(num.type(), "-" + num.value()));
                } else if (Character.isDigit(c)) {
                    tokens.add(readNumberOrDate());
                } else if (Character.isLetter(c) || c == '_') {
                    tokens.add(readIdentifierOrKeyword());
                } else if (c == '>' && pos + 1 < input.length() && input.charAt(pos + 1) == '=') {
                    tokens.add(new Token(TT.GTE, ">=")); pos += 2;
                } else if (c == '<' && pos + 1 < input.length() && input.charAt(pos + 1) == '=') {
                    tokens.add(new Token(TT.LTE, "<=")); pos += 2;
                } else if (c == '!' && pos + 1 < input.length() && input.charAt(pos + 1) == '=') {
                    tokens.add(new Token(TT.NEQ, "!=")); pos += 2;
                } else if (c == '>') {
                    tokens.add(new Token(TT.GT, ">")); pos++;
                } else if (c == '<') {
                    tokens.add(new Token(TT.LT, "<")); pos++;
                } else if (c == '=') {
                    tokens.add(new Token(TT.EQ, "=")); pos++;
                } else if (c == '(') {
                    tokens.add(new Token(TT.LPAREN, "(")); pos++;
                } else if (c == ')') {
                    tokens.add(new Token(TT.RPAREN, ")")); pos++;
                } else {
                    throw new RuleParseException(
                            "Unexpected character '" + c + "' at position " + pos);
                }
            }
            tokens.add(new Token(TT.EOF, "<end>"));
            return tokens;
        }

        private void skipWhitespace() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
                pos++;
            }
        }

        private Token readString(char quote) {
            pos++; // skip opening quote
            var sb = new StringBuilder();
            while (pos < input.length() && input.charAt(pos) != quote) {
                if (input.charAt(pos) == '\\' && pos + 1 < input.length()) {
                    pos++; // skip backslash
                }
                sb.append(input.charAt(pos++));
            }
            if (pos >= input.length()) {
                throw new RuleParseException("Unterminated string literal");
            }
            pos++; // skip closing quote
            return new Token(TT.STRING_VAL, sb.toString());
        }

        private Token readNumberOrDate() {
            // Try to detect date pattern YYYY-MM-DD by lookahead
            if (pos + 10 <= input.length()) {
                var slice = input.substring(pos, pos + 10);
                if (slice.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    pos += 10;
                    // Make sure the next char is not a digit (guard against YYYY-MM-DDT...)
                    if (pos >= input.length() || !Character.isDigit(input.charAt(pos))) {
                        return new Token(TT.DATE_VAL, slice);
                    }
                    pos -= 10; // not a date — fall through to number
                }
            }
            var sb = new StringBuilder();
            while (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
                sb.append(input.charAt(pos++));
            }
            var val = sb.toString();
            if (val.isEmpty()) {
                throw new RuleParseException("Expected a numeric value at position " + pos);
            }
            return new Token(TT.NUMBER_VAL, val);
        }

        private Token readIdentifierOrKeyword() {
            var sb = new StringBuilder();
            while (pos < input.length()
                    && (Character.isLetterOrDigit(input.charAt(pos))
                        || input.charAt(pos) == '_'
                        || input.charAt(pos) == '-')) {
                sb.append(input.charAt(pos++));
            }
            var name = sb.toString();

            // Check if followed by dot → field reference
            if (pos < input.length() && input.charAt(pos) == '.') {
                pos++; // skip the dot
                var sb2 = new StringBuilder();
                while (pos < input.length()
                        && (Character.isLetterOrDigit(input.charAt(pos))
                            || input.charAt(pos) == '_'
                            || input.charAt(pos) == '-')) {
                    sb2.append(input.charAt(pos++));
                }
                if (sb2.isEmpty()) {
                    throw new RuleParseException("Expected field name after '" + name + ".'");
                }
                return new Token(TT.FIELD_REF, name + "." + sb2);
            }

            // Classify as keyword or generic word
            return switch (name.toUpperCase()) {
                case "WHEN"        -> new Token(TT.WHEN,        name);
                case "THEN"        -> new Token(TT.THEN,        name);
                case "AND"         -> new Token(TT.AND,         name);
                case "OR"          -> new Token(TT.OR,          name);
                case "CONTAINS"    -> new Token(TT.CONTAINS,    name);
                case "STARTS_WITH" -> new Token(TT.STARTS_WITH, name);
                case "ENDS_WITH"   -> new Token(TT.ENDS_WITH,   name);
                case "TRUE"        -> new Token(TT.BOOL_VAL,    "true");
                case "FALSE"       -> new Token(TT.BOOL_VAL,    "false");
                default            -> new Token(TT.WORD,        name);
            };
        }
    }
}

