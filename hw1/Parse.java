import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

class ParseException extends Exception {
    ParseException() {
        super("Parse error");
    }
}

public class Parse {
    private static final List<String> TokenList =
            Arrays.asList("{", "}", "System.out.println", "(", ")", ";", "if", "else", "while", "true", "false", "!");

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String s;
        StringBuilder contents = new StringBuilder();
        while ((s = br.readLine()) != null) {
            contents.append(s);
            contents.append(" ");
        }
        br.close();

        try {
            if (parsable(tokenize(contents.toString()))) {
                System.out.println("Program parsed successfully");
            } else {
                throw new ParseException();
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
    }

    private static List<String> tokenize(String s) throws ParseException {
        List<String> tokens = new ArrayList<>();
        while (!s.trim().isEmpty()) {
            String cur = s;
            List<String> matches = TokenList.stream()
                    .filter((String token) -> cur.trim().startsWith(token))
                    .collect(Collectors.toList());
            if (matches.isEmpty()) {
                throw new ParseException();
            } else {
                tokens.add(matches.get(0));
                s = cur.trim().substring(matches.get(0).length());
            }
        }
        return tokens;
    }

    private static boolean parsable(List<String> tokens) {
        Stack<String> stack = new Stack<>();
        stack.push("S");
        while (!tokens.isEmpty()) {
            if (stack.isEmpty())
                return false;
            if (tokens.get(0).equals(stack.peek())) {
                tokens.remove(0);
                stack.pop();
            } else if (stack.peek().equals("S") && tokens.get(0).equals("{")) {
                stack.pop();
                stack.push("}");
                stack.push("L");
                stack.push("{");
            } else if (stack.peek().equals("S") && tokens.get(0).equals("System.out.println")) {
                stack.pop();
                stack.push(";");
                stack.push(")");
                stack.push("E");
                stack.push("(");
                stack.push("System.out.println");
            } else if (stack.peek().equals("S") && tokens.get(0).equals("if")) {
                stack.pop();
                stack.push("S");
                stack.push("else");
                stack.push("S");
                stack.push(")");
                stack.push("E");
                stack.push("(");
                stack.push("if");
            } else if (stack.peek().equals("S") && tokens.get(0).equals("while")) {
                stack.pop();
                stack.push("S");
                stack.push(")");
                stack.push("E");
                stack.push("(");
                stack.push("while");
            } else if (stack.peek().equals("L") && (tokens.get(0).equals("{") ||
                    tokens.get(0).equals("System.out.println") ||
                    tokens.get(0).equals("if") ||
                    tokens.get(0).equals("while"))) {
                stack.pop();
                stack.push("L");
                stack.push("S");
            } else if (stack.peek().equals("L") && tokens.get(0).equals("}")) {
                stack.pop();
            } else if (stack.peek().equals("E") && tokens.get(0).equals("true")) {
                stack.pop();
                stack.push("true");
            } else if (stack.peek().equals("E") && tokens.get(0).equals("false")) {
                stack.pop();
                stack.push("false");
            } else if (stack.peek().equals("E") && tokens.get(0).equals("!")) {
                stack.pop();
                stack.push("E");
                stack.push("!");
            } else {
                return false;
            }
        }
        return stack.isEmpty();
    }
}
