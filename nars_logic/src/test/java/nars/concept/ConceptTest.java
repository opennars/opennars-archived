package nars.concept;

import nars.Narsese;
import nars.nar.Default;
import nars.term.Term;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 11/19/15.
 */
public class ConceptTest {

    @Test
    public void testConceptInstancing() throws Narsese.NarseseException {
        Default n = new Default();

        String statement1 = "<a --> b>.";

        Term a = n.term("a");
        assertTrue(a != null);
        Term a1 = n.term("a");
        assertTrue(a.equals(a1));

        n.input(statement1);
        n.frame(4);

        n.input(" <a  --> b>.  ");
        n.frame(1);
        n.input(" <a--> b>.  ");
        n.frame(1);

        String statement2 = "<a --> c>.";
        n.input(statement2);
        n.frame(4);

        Term a2 = n.term("a");
        assertTrue(a2 != null);

        Concept ca = n.concept(a2);
        assertTrue(ca != null);

        assertEquals(true, n.core.active.size() > 0);

    }

}
