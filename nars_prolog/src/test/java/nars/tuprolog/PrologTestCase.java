package nars.tuprolog;


import junit.framework.TestCase;
import nars.tuprolog.event.SpyEvent;
import nars.tuprolog.event.SpyListener;

public class PrologTestCase extends TestCase {
	
	public void testEngineInitialization() throws InvalidLibraryException {
		Prolog engine = new DefaultProlog();
		assertEquals(String.join(",",engine.getCurrentLibraries()), 4, engine.getCurrentLibraries().length);
		assertNotNull(engine.getLibrary("nars.tuprolog.lib.BasicLibrary"));
		assertNotNull(engine.getLibrary("nars.tuprolog.lib.ISOLibrary"));
		assertNotNull(engine.getLibrary("nars.tuprolog.lib.IOLibrary"));
		assertNotNull(engine.getLibrary("nars.tuprolog.lib.JavaLibrary"));
	}
	
	public void testLoadLibraryAsString() throws InvalidLibraryException {
		Prolog engine = new DefaultProlog();
		engine.loadLibrary("nars.tuprolog.StringLibrary");
		assertNotNull(engine.getLibrary("nars.tuprolog.StringLibrary"));
	}
	
	public void testLoadLibraryAsObject() throws InvalidLibraryException {
		Prolog engine = new DefaultProlog();
		Library stringLibrary = new StringLibrary();
		engine.loadLibrary(stringLibrary);
		assertNotNull(engine.getLibrary("nars.tuprolog.StringLibrary"));
		Library javaLibrary = new nars.tuprolog.lib.JavaLibrary();
		engine.loadLibrary(javaLibrary);
		assertSame(javaLibrary, engine.getLibrary("nars.tuprolog.lib.JavaLibrary"));
	}
	
	public void testGetLibraryWithName() throws InvalidLibraryException {
		Prolog engine = new DefaultProlog(new String[] {"nars.tuprolog.TestLibrary"});
		assertNotNull(engine.getLibrary("TestLibraryName"));
	}
	
	public void testUnloadLibraryAfterLoadingTheory() throws Exception {
		Prolog engine = new DefaultProlog();
		assertNotNull(engine.getLibrary("nars.tuprolog.lib.IOLibrary"));
		Theory t = new Theory("a(1).\na(2).\n");
		engine.setTheory(t);
		engine.unloadLibrary("nars.tuprolog.lib.IOLibrary");
		assertNull(engine.getLibrary("nars.tuprolog.lib.IOLibrary"));
	}
	
	public void testAddTheory() throws InvalidTheoryException, InvalidLibraryException {
		Prolog engine = new DefaultProlog();
		Theory t = new Theory("test :- notx existing(s).");
		try {
			engine.addTheory(t);
			fail();
		} catch (InvalidTermException expected) {
			assertEquals("", engine.getDynamicTheoryCopy().toString());
		}
	}
	
	public void testSpyListenerManagement() throws InvalidLibraryException {
		Prolog engine = new DefaultProlog();
		SpyListener listener1 = new SpyListener() {
			public void onSpy(SpyEvent e) {}
		};
		SpyListener listener2 = new SpyListener() {
			public void onSpy(SpyEvent e) {}
		};
		engine.addSpyListener(listener1);
		engine.addSpyListener(listener2);
		assertEquals(2, engine.getSpyListenerList().size());
	}
	
	public void testLibraryListener() throws InvalidLibraryException {
		Prolog engine = new DefaultProlog(new String[]{});
		engine.loadLibrary("nars.tuprolog.lib.BasicLibrary");
		engine.loadLibrary("nars.tuprolog.lib.IOLibrary");
		TestPrologEventAdapter a = new TestPrologEventAdapter();
		engine.addLibraryListener(a);
		engine.loadLibrary("nars.tuprolog.lib.JavaLibrary");
		assertEquals("nars.tuprolog.lib.JavaLibrary", a.firstMessage);
		engine.unloadLibrary("nars.tuprolog.lib.JavaLibrary");
		assertEquals("nars.tuprolog.lib.JavaLibrary", a.firstMessage);
	}
	
	public void testTheoryListener() throws InvalidTheoryException, InvalidLibraryException {
		Prolog engine = new DefaultProlog();
		TestPrologEventAdapter a = new TestPrologEventAdapter();
		engine.addTheoryListener(a);
		Theory t = new Theory("a(1).\na(2).\n");
		engine.setTheory(t);
		assertEquals("", a.firstMessage);
		assertEquals("a(1).\n\na(2).\n\n", a.secondMessage);
		t = new Theory("a(3).\na(4).\n");
		engine.addTheory(t);
		assertEquals("a(1).\n\na(2).\n\n", a.firstMessage);
		assertEquals("a(1).\n\na(2).\n\na(3).\n\na(4).\n\n", a.secondMessage);
	}
	
	public void testQueryListener() throws Exception {
		Prolog engine = new DefaultProlog();
		TestPrologEventAdapter a = new TestPrologEventAdapter();
		engine.addQueryListener(a);
		engine.setTheory(new Theory("a(1).\na(2).\n"));
		engine.solve("a(X).");
		assertEquals("a(X)", a.firstMessage);
		assertEquals("yes.\nX / 1", a.secondMessage);
		engine.solveNext();
		assertEquals("a(X)", a.firstMessage);
		assertEquals("yes.\nX / 2", a.secondMessage);
	}

}
