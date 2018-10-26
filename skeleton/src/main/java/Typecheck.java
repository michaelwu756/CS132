import typecheck.Helper;
import typecheck.TypeCheckVisitor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Typecheck {

    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder contents = new StringBuilder();
        String s;
        try {
            while ((s = br.readLine()) != null) {
                contents.append(s);
                contents.append("\n");
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new MiniJavaParser(new ByteArrayInputStream(contents.toString().getBytes()));
        try {
            Helper.init(MiniJavaParser.Goal());
            TypeCheckVisitor visitor = new TypeCheckVisitor();
            MiniJavaParser.ReInit(new ByteArrayInputStream(contents.toString().getBytes()));
            visitor.visit(MiniJavaParser.Goal());
            if (visitor.correctlyTypeChecks()) {
                System.out.println("Program type checked successfully");
            } else {
                System.out.println("Type error");
            }
        } catch (ParseException e) {
            System.out.println("Parse error");
            e.printStackTrace();
        }
    }

}
