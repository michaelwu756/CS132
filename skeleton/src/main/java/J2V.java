import syntaxtree.Goal;
import translation.MiniJavaToVaporVisitor;
import translation.TranslationHelper;
import typecheck.Helper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class J2V {

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
            Goal g = MiniJavaParser.Goal();
            TranslationHelper.init(g);
            Helper.init(g);
            MiniJavaToVaporVisitor visitor = new MiniJavaToVaporVisitor();
            visitor.visit(g, null);
            System.out.print(visitor.outputTranslation());
        } catch (ParseException e) {
            System.out.println("Parse error");
            e.printStackTrace();
        }
    }
}