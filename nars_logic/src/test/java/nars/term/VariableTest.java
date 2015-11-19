package nars.term;

import nars.$;
import nars.NAR;
import nars.nal.nal4.Product;
import nars.nar.Terminal;
import nars.task.Task;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 8/28/15.
 */
public class VariableTest {


    @Test
    public void testPatternVarVolume() {

        assertEquals(0, $.$("$x").complexity());
        assertEquals(1, $.$("$x").volume());

        assertEquals(0, $.$("%x").complexity());
        assertEquals(1, $.$("%x").volume());

        assertEquals($.$("<x --> y>").volume(),
                $.$("<%x --> %y>").volume());

    }

    @Test public void testNumVars() {
        assertEquals(1, $.$("$x").vars());
        assertEquals(1, $.$("#x").vars());
        assertEquals(1, $.$("?x").vars());
        assertEquals(0, $.$("%x").vars());

        //the pattern variable is not counted toward # vars
        assertEquals(1, $.$("<$x <-> %y>").vars());
    }

    @Test
    public void testIndpVarNorm() {
        assertEquals(2, $.$("<$x <-> $y>").vars());

        testIndpVarNorm("$x", "$y", "($1, $2)");
        testIndpVarNorm("$x", "$x", "($1, $1)");
        testIndpVarNorm("$x", "#x", "($1, #2)");
        testIndpVarNorm("#x", "#x", "(#1, #1)");
    }

    @Test
    public void testIndpVarNormCompound() {
        //testIndpVarNorm("<$x <-> $y>", "<$x <-> $y>", "(<$1 <-> $2>, <$3 <-> $4>)");

        testIndpVarNorm("$x", "$x", "($1, $1)");
        testIndpVarNorm("#x", "#x", "(#1, #1)");
        testIndpVarNorm("<#x <-> #y>", "<#x <-> #y>", "(<#1 <-> #2>, <#1 <-> #2>)");
        testIndpVarNorm("<$x <-> $y>", "<$x <-> $y>", "(<$1 <-> $2>, <$1 <-> $2>)");
    }
    public void testIndpVarNorm(String vara, String varb, String expect) {


        Term a = $.$(vara);
        Term b = $.$(varb);
        //System.out.println(a + " " + b + " "  + Product.make(a, b).normalized().toString());
      assertEquals(
            expect,
            Product.make(a, b).normalized().toString()
        );
    }

    //    @Test public void testTransformVariables() {
//        NAR nar = new Default();
//        Compound c = nar.term("<$a --> x>");
//        Compound d = Compound.transformIndependentToDependentVariables(c).normalized();
//        assertTrue(c!=d);
//        assertEquals(d, nar.term("<#1 --> x>"));
//    }

    @Test
    public void testDestructiveNormalization() {
        String t = "<$x --> y>";
        String n = "<$1 --> y>";
        NAR nar = new Terminal();
        Term x = nar.term(t);
        assertEquals(n, x.toString());
        //assertTrue("immediate construction of a term from a string should automatically be normalized", x.isNormalized());

    }




//    public void combine(String a, String b, String expect) {
//        NAR n = new Default();
//        Term ta = n.term(a);
//        Term tb = n.term(b);
//        Term c = Conjunction.make(ta, tb).normalized();
//
//        Term e = n.term(expect).normalized();
//        Term d = e.normalized();
//        assertNotNull(e);
//        assertEquals(d, c);
//        assertEquals(e, c);
//    }

    @Test public void varNormTestIndVar() {
        //<<($1, $2) --> bigger> ==> <($2, $1) --> smaller>>. gets changed to this: <<($1, $4) --> bigger> ==> <($2, $1) --> smaller>>. after input
        
        String t = "<<($1, $2) --> bigger> ==> <($2, $1) --> smaller>>";

        Term term = n.term(t);
        Task task = n.task(t + ".");
        //n.input("<<($1, $2) --> bigger> ==> <($2, $1) --> smaller>>.");

        System.out.println(t);
        System.out.println(term);
        System.out.println(task);

        task = task.normalized();
        System.out.println(task);

        Task t2 = n.inputTask(t + ".");
        System.out.println(t2);

        //TextOutput.out(n);
        n.frame(10);

    }

    final Terminal n = new Terminal();

    public void test(String term, int[] v1Index, int[] v2Index) {
        //test for re-use of variable instances during normalization

        Task t = n.inputTask(term + ".");
        Compound ct = t.getTerm();

        Term varA = ct.subterm(v1Index);
        assertTrue(varA instanceof Variable);


        Term varB = ct.subterm(v2Index);
        assertTrue(varB instanceof Variable);

        assertEquals(varA, varB);
        assertTrue("successfull re-use of variable instance", varA==varB);
    }

    @Test
    public void reuseVariableTermsDuringNormalization() {
        test("<<$1 --> x> ==> <$1 --> y>>", new int[] { 0, 0 }, new int[] { 1, 0 });
        //test("<<#1 --> x> ==> <#2 --> y>>", new int[] { 0, 0 }, new int[] { 1, 0 });
        test("<<?x --> x> ==> <?x --> y>>", new int[] { 0, 0 }, new int[] { 1, 0 });
    }

    @Test public void testUnmixed1() {

        {
            String x = "(<(%1, %2) --> z>, <%1 --> %4>)";
            Term y = n.term(x);
            assertEquals(x, y.toString(true));
        }
        {
            String x = "(<(%1, %2) --> %3>, <%1 --> %4>)";
            Term y = n.term(x);
            assertEquals(x, y.toString(true));
        }

    }
    @Test public void testMixed1() {
        {
            String x = "(<(%1, %2) --> ?3>, <%1 --> %4>)";
            Term y = n.term(x);
            assertEquals(x, y.toString(true));
        }
    }

}
