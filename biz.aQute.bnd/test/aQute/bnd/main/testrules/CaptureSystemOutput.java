package aQute.bnd.main.testrules;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.rules.ExternalResource;

public class CaptureSystemOutput extends ExternalResource {

	private final ByteArrayOutputStream	capturedStdOut	= new ByteArrayOutputStream();
	private PrintStream					originalStdOut;

	private final ByteArrayOutputStream	capturedStdErr	= new ByteArrayOutputStream();
	private PrintStream					originalStdErr;

	@Override
	protected void before() throws Throwable {
		originalStdOut = System.out;
		System.setOut(new PrintStream(capturedStdOut));

		originalStdErr = System.err;
		System.setErr(new PrintStream(capturedStdErr));
	}

	@Override
	protected void after() {
		System.setErr(originalStdErr);
		System.setOut(originalStdOut);
	}

	public String getSystemOut() {
		return capturedStdOut.toString()
			.trim();
	}

	public String getSystemErr() {
		return capturedStdErr.toString()
			.trim();
	}
}
