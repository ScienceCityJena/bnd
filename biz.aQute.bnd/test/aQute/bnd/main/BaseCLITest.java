package aQute.bnd.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;

import aQute.bnd.osgi.About;
import aQute.bnd.osgi.Jar;

public class BaseCLITest {

	private final ByteArrayOutputStream	capturedStdOut	= new ByteArrayOutputStream();
	private PrintStream					originalStdOut;

	private final ByteArrayOutputStream	capturedStdErr	= new ByteArrayOutputStream();
	private PrintStream					originalStdErr;
	private String						version;

	@Before
	public void setUp() throws Exception {
		version = About.CURRENT.getWithoutQualifier()
			.toString();

		capturedStdOut.reset();
		originalStdOut = System.out;
		System.setOut(new PrintStream(capturedStdOut));

		capturedStdErr.reset();
		originalStdErr = System.err;
		System.setErr(new PrintStream(capturedStdErr));
	}

	@After
	public void tearDown() throws Exception {
		System.setErr(originalStdErr);
		System.setOut(originalStdOut);
	}

	private void expectOutput(String expected) {
		assertEquals("wrong output", expected, capturedStdOut.toString()
			.trim());
	}

	private void expectNoError() {
		assertEquals("non-empty error output", "", capturedStdErr.toString()
			.trim());
	}

	private void expectJarEntry(Jar jar, String path) {
		assertNotNull("missing entry in jar: " + path, jar.getResource(path));
	}

}
