import typecheck.Helper;
import typecheck.TypeCheckVisitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Typecheck {

    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        new MiniJavaParser(br);
        try {
            Helper.init(MiniJavaParser.Goal());
            TypeCheckVisitor visitor = new TypeCheckVisitor();
            visitor.visit(MiniJavaParser.Goal());
            if (visitor.correctlyTypeChecks()) {
                System.out.println("Program type checked successfully");
            } else {
                System.out.println("Type error");
            }
        } catch (ParseException e) {
            System.out.println("Parse error");
        }
    }

}
