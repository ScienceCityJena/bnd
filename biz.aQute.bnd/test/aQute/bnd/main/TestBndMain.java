package aQute.bnd.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
	private String						testdataDir		= "testdata/";
	private Path						gTestPath;
	private Path						gTData;
	private Path						gTD_Bu;
	private Path						gTD_SA;
	private Path						gTD_WS;

	@Before
	public void setUp() throws Exception {

		gTestPath = Paths.get("generated/tmp/test/" + testName.getMethodName())
			.toAbsolutePath();
		gTData = gTestPath.resolve(testdataDir)
			.toAbsolutePath();
		gTD_Bu = gTData.resolve("bundles")
			.toAbsolutePath();
		gTD_SA = gTData.resolve("standalone")
			.toAbsolutePath();
		gTD_WS = gTData.resolve("workspace")
			.toAbsolutePath();

		IO.delete(gTData);
		IO.copy(IO.getFile(testdataDir), gTData.toFile());

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
			"run", "standalone/standalone.bndrun"
		}, gTData);
		expectNoError();
		expectOutput("Gesundheit!");
	}

	@Test
	public void testRunWorkspace() throws Exception {
		bnd.mainNoExit(new String[] {
			"run", "workspace/p/workspace.bndrun"
		}, gTData);
		expectNoError();
		expectOutput("Gesundheit!");
	}

	@Test
	public void testPackageBndrunStandalone() throws Exception {
		String output = gTestPath + "/export-standalone.jar";
		bnd.mainNoExit(new String[] {
			"package", "-o", output, "standalone/standalone.bndrun"
		}, gTData);
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
		String output = gTestPath + "export-workspace.jar";
		bnd.mainNoExit(new String[] {
			"package", "-o", output, "workspace/p/workspace.bndrun"
		}, gTData);
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
		String output = gTData.resolve("export-workspace-project.jar")
			.toString();
		bnd.mainNoExit(new String[] {
			"package", "-o", output, "workspace/p2"
		}, gTData);
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

	List<Path>	addedPaths		= new ArrayList<>();
	List<Path>	unremovedPaths	= new ArrayList<>();
	List<Path>	removedPaths	= new ArrayList<>();
	List<Path>	modifiedPaths	= new ArrayList<>();

	public void test(String input, Path baseExecDir) throws Exception {

		List<Path> filesBefore = Files.walk(gTData)
			.filter(Files::isRegularFile)
			.collect(Collectors.toList());

		long time = System.currentTimeMillis();

		bnd.mainNoExit(input.split(" "), baseExecDir.toAbsolutePath());

		List<Path> modyfiedFiels = Files.walk(gTData)
			.filter(p -> {
				try {
					return Files.getLastModifiedTime(p)
						.toMillis() > time;
				} catch (IOException e) {

					e.printStackTrace();
				}
				return true;
			})
			.collect(Collectors.toList());

		List<Path> filesAfter = Files.walk(gTData)
			.filter(Files::isRegularFile)
			.collect(Collectors.toList());

		addedPaths.addAll(filesAfter);
		filesBefore.forEach(e -> {
			boolean b = addedPaths.remove(e) ? unremovedPaths.add(e) : removedPaths.add(e);
		});

		modifiedPaths = unremovedPaths.stream()
			.filter(p -> {
				try {
					return Files.getLastModifiedTime(p)
						.toMillis() > time;
				} catch (IOException e) {

					e.printStackTrace();
				}
				return true;
			})
			.collect(Collectors.toList());
		printFileInfos();
	}

	private void printFileInfos() throws IOException {

		BufferedWriter bw = Files.newBufferedWriter(gTData.resolve("filechanges.txt"), StandardOpenOption.CREATE_NEW);

		PrintWriter pw = new PrintWriter(bw);

		pw.println("added: " + addedPaths.size());
		for (Path path : addedPaths) {
			pw.println("-" + path);
		}
		pw.println();
		pw.println("unremoved: " + unremovedPaths.size());
		for (Path path : unremovedPaths) {
			pw.println("-" + path);
		}
		pw.println();
		pw.println("removed: " + removedPaths.size());
		for (Path path : removedPaths) {
			pw.println("-" + path);
		}
		pw.println();
		pw.println("modified: " + modifiedPaths.size());
		for (Path path : modifiedPaths) {
			pw.println("-" + path);
		}
		pw.close();
		bw.close();

	}

	@Test
	public void testCompile() throws Exception {
		String input = "compile";

		test(input, gTD_WS);
		expectNoError();

		expectAddedFiles(gTD_WS, "p3/bin/somepackage/SomeClass.class");

		expectFilesCount(1, 0, 0);

	}

	private void expectFilesCount(Integer added, Integer removed, Integer modified) {
		expectFilesCount(added, removed, modified, null);
	}

	private void expectFilesCount(Integer added, Integer removed, Integer modified, Integer unremoved) {

		if (added != null) {
			assertEquals(added, Integer.valueOf(addedPaths.size()));
		}

		if (unremoved != null) {
			assertEquals(unremoved, Integer.valueOf(unremovedPaths.size()));
		}

		if (removed != null) {
			assertEquals(removed, Integer.valueOf(removedPaths.size()));
		}

		if (modified != null) {
			assertEquals(modified, Integer.valueOf(modifiedPaths.size()));
		}
	}

	@Test
	public void testClean() throws Exception {
		String input = "clean";

		test(input, gTD_WS);
		expectNoError();

		expectRemovedFiles(gTD_WS, "p2/generated/buildfiles");
		expectRemovedFiles(gTD_WS, "p2/generated/p2.jar");
		expectRemovedFiles(gTD_WS, "p3/bin/somepackage/SomeOldClass.class");

		expectFilesCount(0, 3, 0);
	}

	private void expectRemovedFiles(Path base, String file) {
		assertTrue(removedPaths.contains(base.resolve(file)));
	}

	private void expectAddedFiles(Path base, String file) {
		assertTrue(addedPaths.contains(base.resolve(file)));
	}

	private void expectModifiedFiles(Path base, String file) {
		assertTrue(modifiedPaths.contains(base.resolve(file)));
	}
}
