import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Typecheck {

    public static void main (String [] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        MiniJavaParser parser = new MiniJavaParser(br);
        try{
            TypecheckVisitor visitor = new TypecheckVisitor();
            visitor.visit(MiniJavaParser.Goal());
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Program type checked successfully");
    }

}
