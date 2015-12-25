package dk.brics.automaton;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by me on 12/25/15.
 */
public class TokenAutomatonTest {

    @Test
    public void testMatch() {
        final Object TOKEN_ID = new Object();
        final AbstractAutomaton a = new TokenRegExp("ABC", TOKEN_ID).toAutomaton();
        final TokenAutomaton ta = new TokenAutomaton(a);
        final String input = "ABC";
        final TokenDetails details = new TokenDetails();
        final boolean match = ta.find(input, 0, false, details);

        assertTrue("match", match);
        assertSame("details.seq", input, details.seq);
        assertEquals("details.off", 0, details.off);
        assertEquals("details.len", 3, details.len);
        assertSame("details.info", TOKEN_ID, details.info);
    }

    @Test
    public void testMatchWithContext() {
        final Object TOKEN_ID = new Object();
        final AbstractAutomaton a = new TokenRegExp("ABC", TOKEN_ID).toAutomaton();
        final TokenAutomaton ta = new TokenAutomaton(a);
        final String input = "XABCX";
        final TokenDetails details = new TokenDetails();
        final boolean match = ta.find(input, 1, false, details);

        assertTrue("match", match);
        assertSame("details.seq", input, details.seq);
        assertEquals("details.off", 1, details.off);
        assertEquals("details.len", 3, details.len);
        assertSame("details.info", TOKEN_ID, details.info);
    }

    @Test
    public void testNoMatch() {
        final Object TOKEN_ID = new Object();
        final AbstractAutomaton a = new TokenRegExp("ABC", TOKEN_ID).toAutomaton();
        final TokenAutomaton ta = new TokenAutomaton(a);
        final String input = "XYZ";
        final TokenDetails details = new TokenDetails();
        final boolean match = ta.find(input, 0, false, details);

        assertFalse("match", match);
        assertSame("details.info", TokenDetails.NO_MATCH, details.info);
    }

    @Test
    public void testUnderflow() {
        final Object TOKEN_ID = new Object();
        final AbstractAutomaton a = new TokenRegExp("ABC(DE)?", TOKEN_ID).toAutomaton();
        final TokenAutomaton ta = new TokenAutomaton(a);
        final String input = "ABC";
        final TokenDetails details = new TokenDetails();
        final boolean match = ta.find(input, 0, false, details);

        assertSame("details.info", TokenDetails.UNDERFLOW, details.info);
        assertFalse("match", match);
    }

    @Test
    public void testUnderflowWithEndOfInput() {
        final Object TOKEN_ID = new Object();
        final AbstractAutomaton a = new TokenRegExp("ABC(DE)?", TOKEN_ID).toAutomaton();
        final TokenAutomaton ta = new TokenAutomaton(a);
        final String input = "ABC";
        final TokenDetails details = new TokenDetails();
        final boolean match = ta.find(input, 0, true, details);

        assertSame("details.seq", input, details.seq);
        assertEquals("details.off", 0, details.off);
        assertEquals("details.len", 3, details.len);
        assertSame("details.info", TOKEN_ID, details.info);
        assertTrue("match", match);
    }

    @Test
    public void testPriority() {
        final Object KEYWORD_FOO = "KEYWORD_FOO";
        final Object IDENTIFIER = "IDENTIFIER";
        final AbstractAutomaton a = new TokenRegExp("foo", KEYWORD_FOO).toAutomaton(true);
        final AbstractAutomaton b = new TokenRegExp("[a-z]+", IDENTIFIER).toAutomaton(true);
        final AbstractAutomaton c =b.minus(a).union(a);


        System.out.println(a.info);
        System.out.println(b.info);
        System.out.println(c.info);

        final TokenAutomaton ta = new TokenAutomaton(c);
        final String input = "foo bar";
        final TokenDetails details = new TokenDetails();
        final boolean match = ta.find(input, 0, false, details);


        System.out.println(details.toString());

        assertSame("details.seq", input, details.seq);
        assertEquals("details.off", 0, details.off);
        assertEquals("details.len", 3, details.len);
        assertSame("details.info", KEYWORD_FOO, details.info);
        assertTrue("match", match);

    }

}