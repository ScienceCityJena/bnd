package aQute.bnd.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;

import org.junit.Rule;
import org.junit.Test;

import aQute.bnd.main.testrules.CaptureSystemOutput;
import aQute.bnd.main.testrules.WatchedFolder;
import aQute.bnd.main.testrules.WatchedFolder.FileStatus;
import aQute.bnd.main.testrules.WatchedTemporaryFolder;
import aQute.bnd.osgi.Jar;

public class TestBndMain2 {

	@Rule
	public WatchedFolder							folder					= new WatchedTemporaryFolder();

	@Rule
	public CaptureSystemOutput						capturedStdIO			= new CaptureSystemOutput();

	protected static final Function<Path, String>	DEFAULT_SNAPSHOT_FUNC	= p -> String.valueOf(p.toFile()
		.lastModified());

	protected static final String					TESTDATA_BASE_DIR		= "testdata";

	protected static final String					BUNDLES					= "bundles";

	protected static final String					STANDALONE				= "standalone";

	protected static final String					WORKSPACE				= "workspace";

	@Test
	public void testRunStandalone() throws Exception {
		initTestData(STANDALONE);
		execBndCmd("run", "standalone.bndrun");
		// expectNoError();
		expectOutput("Gesundheit!");
	}

	/* cmd */
	protected void execBndCmd(String... cmd) throws Exception {
		bnd.mainNoExit(cmd, folder.getRootPath());
	}

	/* Folder based helper */
	protected void initTestData(final String subdir) throws IOException {
		folder.copyDataFrom(Paths.get(TESTDATA_BASE_DIR, subdir))
			.snapshot(DEFAULT_SNAPSHOT_FUNC);
	}

	protected void checkFileStataus(FileStatus expectedFileStatus, String... p) {
		assertEquals(expectedFileStatus, folder.checkFile(Paths.get("", p)));
	}

	protected long countFilesWithStatus(final FileStatus fileStatus) throws IOException {
		final Map<Path, FileStatus> stat = folder.createFileStatistic(true);

		return stat.values()
			.stream()
			.filter(s -> s == FileStatus.UNMODIFIED_EXISTS)
			.count();
	}

	/* IO based helper */
	protected void expectOutput(String expected) {
		assertEquals("wrong output", expected, capturedStdIO.getSystemOut());
	}

	protected void expectNoError() {
		assertEquals("non-empty error output", "", capturedStdIO.getSystemErr());
	}

	protected void expectJarEntry(Jar jar, String path) {
		assertNotNull("missing entry in jar: " + path, jar.getResource(path));
	}
}
