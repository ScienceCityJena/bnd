package aQute.bnd.main;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;

import org.junit.Rule;

import aQute.bnd.main.testrules.WatchedFolder;
import aQute.bnd.main.testrules.WatchedFolder.FileStatus;
import aQute.bnd.main.testrules.WatchedTemporaryFolder;

public class BaseBndTest {

	@Rule
	public WatchedFolder							folder					= new WatchedTemporaryFolder();

	protected static final Function<Path, String>	DEFAULT_SNAPSHOT_FUNC	= p -> String.valueOf(p.toFile()
		.lastModified());

	protected static final String					TESTDATA_BASE_DIR		= "testdata";

	protected static final String					BUNDLES					= "bundles";

	protected static final String					STANDALONE				= "standalone";

	protected static final String					WORKSPACE				= "workspace";

	/* command execution helper */
	protected String[] execBndCmd(String... cmdWithArgs) throws Exception {
		ByteArrayOutputStream sysOutCaptured = new ByteArrayOutputStream();
		ByteArrayOutputStream sysErrCaptured = new ByteArrayOutputStream();

		PrintStream sysOutOrig = System.out;
		PrintStream sysErrOrig = System.err;

		System.setOut(new PrintStream(sysOutCaptured));
		System.setErr(new PrintStream(sysErrCaptured));

		bnd.mainNoExit(cmdWithArgs, folder.getRootPath());

		// recover
		System.setOut(sysOutOrig);
		System.setErr(sysErrOrig);

		return new String[] {
			sysOutCaptured.toString()
				.trim(),
			sysErrCaptured.toString()
				.trim()
		};
	}

	protected void initTestData(final String subdir) throws IOException {
		folder.copyDataFrom(Paths.get(TESTDATA_BASE_DIR, subdir))
			.snapshot(DEFAULT_SNAPSHOT_FUNC);
	}

	/* assertions */
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

	//
	// private void expectOutput(String expected) {
	// assertEquals("wrong output", expected, capturedStdOut.toString()
	// .trim());
	// }
	//
	// private void expectNoError() {
	// assertEquals("non-empty error output", "", capturedStdErr.toString()
	// .trim());
	// }

}
