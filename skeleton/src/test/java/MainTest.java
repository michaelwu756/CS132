import org.junit.BeforeClass;
import org.junit.Test;
import typecheck.Helper;
import typecheck.LinkSetPair;
import typecheck.MethodSignature;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static junit.framework.TestCase.*;
import static typecheck.Helper.*;

public class MainTest {
    @BeforeClass
    public static void init() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        new MiniJavaParser(br);
    }

    @Test
    public void testAppHasAGreeting() {
        Typecheck classUnderTest = new Typecheck();
        assertTrue(true);
    }

    @Test
    public void detectsAcyclic() {
        Set<LinkSetPair> linkSetPairs = new HashSet<>();
        linkSetPairs.add(new LinkSetPair("1", "2"));
        linkSetPairs.add(new LinkSetPair("2", "3"));
        assertTrue(acyclic(linkSetPairs));
    }

    @Test
    public void detectsCyclic() {
        Set<LinkSetPair> linkSetPairs = new HashSet<>();
        linkSetPairs.add(new LinkSetPair("1", "2"));
        linkSetPairs.add(new LinkSetPair("2", "3"));
        linkSetPairs.add(new LinkSetPair("3", "1"));
        assertFalse(acyclic(linkSetPairs));
    }

    @Test
    public void detectsSelfCyclic() {
        Set<LinkSetPair> linkSetPairs = new HashSet<>();
        linkSetPairs.add(new LinkSetPair("1", "2"));
        linkSetPairs.add(new LinkSetPair("2", "3"));
        linkSetPairs.add(new LinkSetPair("3", "3"));
        assertFalse(acyclic(linkSetPairs));
    }

    @Test
    public void detectsDistinct() {
        List<String> ids = new ArrayList<>();
        ids.add("1");
        ids.add("2");
        ids.add("3");
        assertTrue(distinct(ids));
    }

    @Test
    public void detectsNonDistinct() {
        List<String> ids = new ArrayList<>();
        ids.add("1");
        ids.add("1");
        assertFalse(distinct(ids));
    }

    @Test
    public void detectsSubtype() {
        String input =
                "class BubbleSort { " +
                        "   public static void main(String[] a){ }" +
                        "}" +
                        "class A extends B {}";
        InputStream s = new ByteArrayInputStream(input.getBytes());
        MiniJavaParser.ReInit(s);
        try {
            Helper.init(MiniJavaParser.Goal());
        } catch (Exception e) {
            fail();
        }
        assertTrue(subtype("A", "B"));
        assertFalse(subtype("B", "A"));
        assertTrue(subtype("B", "B"));
    }

    @Test
    public void detectsFields() {
        String input =
                "class BubbleSort { " +
                        "   public static void main(String[] a){ }" +
                        "}" +
                        "class B { boolean size; }" +
                        "class A extends B { int[] size; int b;}" +
                        "class C extends A {}" +
                        "class BBS extends B { int[] number; int size; }";
        InputStream s = new ByteArrayInputStream(input.getBytes());
        MiniJavaParser.ReInit(s);
        try {
            Helper.init(MiniJavaParser.Goal());
        } catch (Exception e) {
            fail();
        }
        assertEquals("boolean", fields("B").get("size"));
        assertEquals("int[]", fields("A").get("size"));
        assertEquals(2, fields("C").size());
        assertEquals("int", fields("BBS").get("size"));
    }

    @Test
    public void detectsMethods() {
        String input =
                "class BubbleSort { " +
                        "   public static void main(String[] a){ }" +
                        "}" +
                        "class B { public int a() { return 1; } }" +
                        "class A extends B { public bool a() { return 2; } }" +
                        "class C extends A { public bool b(int f, bool s) { return false; } }";
        InputStream s = new ByteArrayInputStream(input.getBytes());
        MiniJavaParser.ReInit(s);
        try {
            Helper.init(MiniJavaParser.Goal());
        } catch (Exception e) {
            fail();
        }
        assertEquals(new MethodSignature(new ArrayList<>(), "int"), methodType("B", "a"));
        assertEquals(new MethodSignature(new ArrayList<>(), "bool"), methodType("A", "a"));
        assertEquals(new MethodSignature(Arrays.asList("int", "bool"), "bool"), methodType("C", "b"));
        assertEquals(new MethodSignature(new ArrayList<>(), "bool"), methodType("C", "a"));
        assertNull(methodType("B", "b"));
    }

    @Test
    public void detectsOverrides() {
        String input =
                "class BubbleSort { " +
                        "   public static void main(String[] a){ }" +
                        "}" +
                        "class B { public int a() { return 1; } }" +
                        "class A extends B { public bool a() { return 2; } }" +
                        "class C extends A { public bool b(int f, bool s) { return false; } }";
        InputStream s = new ByteArrayInputStream(input.getBytes());
        MiniJavaParser.ReInit(s);
        try {
            Helper.init(MiniJavaParser.Goal());
        } catch (Exception e) {
            fail();
        }
        assertFalse(noOverrides("A", "B", "a"));
    }

    @Test
    public void detectsNoOverrides() {
        String input =
                "class BubbleSort { " +
                        "   public static void main(String[] a){ }" +
                        "}" +
                        "class B { public int a() { return 1; } }" +
                        "class A extends B { public int a() { return 2; } }" +
                        "class C extends A { public bool b(int f, bool s) { return false; } }";
        InputStream s = new ByteArrayInputStream(input.getBytes());
        MiniJavaParser.ReInit(s);
        try {
            Helper.init(MiniJavaParser.Goal());
        } catch (Exception e) {
            fail();
        }
        assertTrue(noOverrides("A", "B", "a"));
    }
}
