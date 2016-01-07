package nars.nal.space;

import nars.$;
import nars.term.TermVector;
import nars.term.atom.Atom;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by me on 1/7/16.
 */
public class SpaceTest {

    @Test public void test1() {
        Atom x = $.the("x");
        Atom y = $.the("y");
        TermVector xy = new TermVector(x, y);
        Space xy00 = new Space(xy, 0, 0);
        assertEquals("(+,x*0.0,y*0.0)", xy00.toString());
        assertEquals(xy00.hash2, new Space(xy, 0, 0).hash2);
        assertEquals(xy00.vector, new Space(xy, 0, 0).vector);
        assertEquals(xy00.subterms(), new Space(xy, 0, 0).subterms());
        assertEquals(xy00, xy00);
        assertEquals(0, xy00.compareTo(xy00));
        assertEquals(0, xy00.compareTo(new Space(xy, 0, 0)));
        assertEquals(xy00, new Space(xy, 0, 0));

        assertEquals(xy.reverse(), new TermVector(y, x)); //TODO move this to TermVctor test

        assertNotEquals(xy00, new Space(xy.reverse(), 0, 0));

        Space xy11 = new Space(xy, 1, 1);
        assertEquals("(+,x*1.0,y*1.0)", xy11.toString());
        assertNotEquals(xy00, xy11);

        assertNotEquals(0, xy11.compareTo(xy00));
        assertEquals(-xy00.compareTo(xy11), xy11.compareTo(xy00));

    }

}