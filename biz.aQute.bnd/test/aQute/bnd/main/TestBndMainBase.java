package aQute.bnd.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Rule;

import aQute.bnd.main.testrules.CapturedSystemOutput;
import aQute.bnd.main.testrules.WatchedFolder;
import aQute.bnd.main.testrules.WatchedFolder.FileStatus;
import aQute.bnd.main.testrules.WatchedTemporaryFolder;
import aQute.bnd.osgi.About;
import aQute.bnd.osgi.Jar;

public class TestBndMainBase {

	@Rule
	public WatchedFolder							folder					= new WatchedTemporaryFolder();

	@Rule
	public CapturedSystemOutput						capturedStdIO			= new CapturedSystemOutput();

	protected static final Function<Path, String>	DEFAULT_SNAPSHOT_FUNC	= p -> String.valueOf(p.toFile()
		.lastModified());

	private static final String						TESTDATA_BASE_DIR		= "testdata";

	protected static final String					BUNDLES					= "bundles";

	protected static final String					STANDALONE				= "standalone";

	protected static final String					WORKSPACE				= "workspace";

	/* BndCmd */
	protected void executeBndCmd(String... cmd) throws Exception {
		bnd.mainNoExit(cmd, folder.getRootPath());
	}

	protected void executeBndCmd(Path subBase, String... cmd) throws Exception {
		bnd.mainNoExit(cmd, folder.getRootPath()
			.resolve(subBase));
	}

	protected String getVersion() {
		return About.CURRENT.getWithoutQualifier()
			.toString();
	}

	/* Folder based helper */
	protected void initTestData(final String subdir) throws IOException {
		folder.copyDataFrom(Paths.get(TESTDATA_BASE_DIR, subdir))
			.snapshot(DEFAULT_SNAPSHOT_FUNC);
	}

	protected void initTestDataAll() throws IOException {
		folder.copyDataFrom(Paths.get("", TESTDATA_BASE_DIR))
			.snapshot(DEFAULT_SNAPSHOT_FUNC);
	}

	protected void expectFileStataus(FileStatus expectedFileStatus, String... p) {
		assertEquals(expectedFileStatus, folder.checkFile(Paths.get("", p)));
	}

	protected Map<FileStatus, Long> countFilesForAllStatus() throws IOException {
		return folder.createFileStatistic(true)
			.values()
			.stream()
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
	}

	protected Long countFilesForStatus(FileStatus fileStatus) throws IOException {
		return count(countFilesForAllStatus(), fileStatus);
	}

	protected void expectFileCounts(Long created, Long modified, Long deleted, Long unmodified) throws IOException {
		final Map<FileStatus, Long> counts = countFilesForAllStatus();

		assertEquals(created, count(counts, FileStatus.CREATED));
		assertEquals(modified, count(counts, FileStatus.MODIFIED));
		assertEquals(deleted, count(counts, FileStatus.DELETED));
		assertEquals(unmodified, count(counts, FileStatus.UNMODIFIED_EXISTS));
	}

	private Long count(Map<FileStatus, Long> counts, FileStatus fileStatus) {
		final Long count = counts.get(fileStatus);
		return count == null ? 0 : count;
	}

	/* IO */
	protected void expectOutput(String expected) {
		assertEquals("wrong output", expected, capturedStdIO.getSystemOutContent());
	}

	protected void expectNoError() {
		assertEquals("non-empty error output", "", capturedStdIO.getSystemErrContent());
	}

	protected void expectJarEntry(Jar jar, String path) {
		assertNotNull("missing entry in jar: " + path, jar.getResource(path));
	}

	/* Print */
	protected void print(final String str) {
		capturedStdIO.getSystemOut()
			.print(str);
	}

	protected void println(final String str) {
		capturedStdIO.getSystemOut()
			.println(str);
	}
}
