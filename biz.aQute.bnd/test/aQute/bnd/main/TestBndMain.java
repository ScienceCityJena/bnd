package aQute.bnd.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import aQute.bnd.osgi.About;
import aQute.bnd.osgi.Jar;
import aQute.lib.io.IO;

public class TestBndMain {

	private final ByteArrayOutputStream	capturedStdOut	= new ByteArrayOutputStream();
	private PrintStream					originalStdOut;

	private final ByteArrayOutputStream	capturedStdErr	= new ByteArrayOutputStream();
	private PrintStream					originalStdErr;
	private String						version;

	@Rule
	public final TestName				testName		= new TestName();
	private String						genTestPath;
	private String						genTestDataPath;
	private String						testdataDir		= "testdata/";

	@Before
	public void setUp() throws Exception {

		genTestPath = "generated/tmp/test/" + testName.getMethodName() + "/";
		genTestDataPath = genTestPath + testdataDir;
		File wsRoot = IO.getFile(genTestDataPath);
		IO.delete(wsRoot);
		IO.copy(IO.getFile(testdataDir), wsRoot);

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

	@Test
	public void testRunStandalone() throws Exception {
		bnd.mainNoExit(new String[] {
			"run", genTestDataPath + "/standalone/standalone.bndrun"
		});
		expectNoError();
		expectOutput("Gesundheit!");
	}

	@Test
	public void testRunWorkspace() throws Exception {
		bnd.mainNoExit(new String[] {
			"run", genTestDataPath + "/workspace/p/workspace.bndrun"
		});
		expectNoError();
		expectOutput("Gesundheit!");
	}

	@Test
	public void testPackageBndrunStandalone() throws Exception {
		String output = genTestPath + "/export-standalone.jar";
		bnd.mainNoExit(new String[] {
			"package", "-o", output, genTestDataPath + "/standalone/standalone.bndrun"
		});
		expectNoError();

		// validate exported jar content
		try (Jar result = new Jar(new File(output))) {
			expectJarEntry(result, "jar/biz.aQute.launcher-" + version + ".jar");
			expectJarEntry(result, "jar/org.apache.felix.framework-5.6.10.jar");
			expectJarEntry(result, "jar/printAndExit-1.0.0.jar");
		}
	}

	@Test
	public void testPackageBndrunWorkspace() throws Exception {
		String output = genTestPath + "export-workspace.jar";
		bnd.mainNoExit(new String[] {
			"package", "-o", output, genTestDataPath + "/workspace/p/workspace.bndrun"
		});
		expectNoError();

		// validate exported jar content
		try (Jar result = new Jar(new File(output))) {
			expectJarEntry(result, "jar/biz.aQute.launcher-" + version + ".jar");
			expectJarEntry(result, "jar/org.apache.felix.framework-5.6.10.jar");
			expectJarEntry(result, "jar/printAndExit-1.0.0.jar");
		}
	}

	@Test
	public void testPackageProject() throws Exception {
		String output = genTestPath + "export-workspace-project.jar";
		bnd.mainNoExit(new String[] {
			"package", "-o", output, genTestDataPath + "/workspace/p2"
		});
		expectNoError();

		// validate exported jar content
		try (Jar result = new Jar(new File(output))) {
			expectJarEntry(result, "jar/biz.aQute.launcher-" + version + ".jar");
			expectJarEntry(result, "jar/org.apache.felix.framework-5.6.10.jar");
			expectJarEntry(result, "jar/p2.jar");
		}
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
