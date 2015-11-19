package nars.term;

import nars.Op;
import nars.term.transform.MatchSubst;
import nars.util.data.random.XorShift1024StarRandom;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Test;

import static nars.$.$;
import static nars.util.Texts.n2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 11/19/15.
 */
public class MatchSubstTest {

    public static final int MAXPOWER = 5000;
    public static final int SEEDS = 5;

    @Test public void testPowerLevels() {
        testPowerLevels("(x,%1)", "(x, y)");
        testPowerLevels("(x,y,%1)", "(x,y,z)");

        testPowerLevels("{x,y,%1}", "{x,y,z}");

        testPowerLevels("({x,y,%1},{a,b,%1})", "({x,y,z},{a,b,x})");

        testPowerLevels("({x,y,%1},{a,b,%2})", "({x,y,z},{a,b,x})");

        testPowerLevels("({x,y,z, %1},{%1,a,b,c})", "({x,y,z,w},{a,b,c,w})");


    }

    @Test public void testDifferentSizeSetBalancing() {

        double d = testSubtermBalancing(
                "({a,b,c,d,%1}, {%1,a})", "({a,b,c,d,z}, {a,z})",
                "({%1,a}, {a,b,c,d,%1})", "({a,z}, {a,b,c,d,z})");

        //correct subterm ordering should make these two
        assertTrue(d >= 0.99f);

        double d2 = testSubtermBalancing(
                "({a,b,c,d,%1}, {%1,a})", "({a,b,c,d,z}, {a,z})",
                "({%1,a}, {a,b,c,d,%1})", "({a,z}, {a,b,c,d,z})");

        //correct subterm ordering should make these two have equal costs
        assertTrue(d2 >= 0.99f);

        assertEquals(d, d2, 0.001);
    }

    @Test public void testProductVsSetSubtermBalancing() {
        double d = testSubtermBalancing(
                "({a,b,c,d,%1}, (%1,a))", "({a,b,c,d,z}, (a,z))",
                "({%1,a}, (a,b,c,d,%1))", "({a,z}, (a,b,c,d,z))");

    }
    @Test public void testSetsAndSets() {
        double d = testSubtermBalancing(
                "{{a,b,c,d,%1}, (%1,a)}", "{{a,b,c,d,z}, (a,z)}",
                "{{%1,a}, (a,b,c,d,%1)}", "{{a,z}, (a,b,c,d,z)}");
    }
    @Test public void testSetsAndSets2() {
        double d = testSubtermBalancing(
                "{{{a,b,c,%1},d}, {%1,a}}", "{{{a,b,c,z},d}, {a,z}}",
                "{{%1,a}, {{a,b,c,%1},d}}", "{{a,z}, {{a,b,c,z},d}}");
    }
    @Test public void testSetsAndSets3() {
        double d = testSubtermBalancing(
                "{{{a,b,%3,%1},%2}, {%1,%4}}", "{{{a,b,c,z},d}, {a,z}}",
                "{{%1,%4}, {{a,b,%3,%1},%2}}", "{{a,z}, {{a,b,c,z},d}}");

        assertTrue(d > 0.99f);
    }

    public double testSubtermBalancing(String frontPat, String frontTerm, String backPat, String backTerm) {
        //assymmetric to test balancing of products


        /*double frontLoad = testPowerLevels("({a,b,c,d,e,%1}, {%1,a})", "({a,b,c,d,e,z}, {a,z})");
        double backLoad = testPowerLevels("({%1,a}, {a,b,c,d,e,%1})", "({a,z}, {a,b,c,d,e,z})");*/

        double frontLoad = testPowerLevels(frontPat, frontTerm);
        double backLoad = testPowerLevels(backPat, backTerm);


        double diff = Math.min(frontLoad, backLoad) / Math.max(frontLoad, backLoad);


        System.out.println("testSubtermBalancing");
        System.out.println("\t" + n2(100.0*diff) + "% |- " + frontLoad + " " + backLoad);
        System.out.println("\n\n");

        //frontLoad and backLoad should be approximate equal if fair

        return diff;
    }

    public double testPowerLevels(String a, String b) {

        Term prod2Pat = $(a);
        MatchSubst.TermPattern pat = new MatchSubst.TermPattern(Op.VAR_PATTERN, prod2Pat);
        Term prod2Ter = $(b);

        SummaryStatistics stat = new SummaryStatistics();

        System.out.println(prod2Pat + " <<<<~>>> " + prod2Ter);

        for (int s = 0; s < SEEDS; s++) {
            for (int p = 1; p < MAXPOWER; p += 1) {
                boolean ok = testPowerSufficiency(s,
                        pat,
                        prod2Ter,
                        p);
                if (ok) {
                    //System.out.println("seed=" + s + "\tpower=" + p);
                    stat.addValue(p);
                    break;
                }
            }
        }

        System.out.println("\tN=" + stat.getN() +
                " mean=" + n2(stat.getMean()) +
                " stddev=" + n2(stat.getStandardDeviation()) +
                " range=" + stat.getMin() + ".." + stat.getMax());

        return stat.getMean();
    }

    public boolean testPowerSufficiency(int seed, MatchSubst.TermPattern t1, Term t2, int p) {
        MatchSubst.State s = MatchSubst.next(new XorShift1024StarRandom(seed),
                t1, t2, p);
        boolean success = s.match();

//        if (!success) {
//            System.err.println(s);
//        }

        return success;
        //assertEquals(expectSuccess, success);
    }
}
