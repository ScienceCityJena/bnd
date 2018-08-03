package aQute.bnd.main;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import org.junit.Rule;
import org.junit.Test;

import aQute.bnd.main.testrules.WatchedFolder;
import aQute.bnd.main.testrules.WatchedFolder.FileStatus;
import aQute.bnd.main.testrules.WatchedTemporaryFolder;

public class BaseTest {

	@Rule
	public WatchedFolder							folder					= new WatchedTemporaryFolder();

	protected static final Function<Path, String>	DEFAULT_SNAPSHOT_FUNC	= p -> String.valueOf(p.toFile()
		.lastModified());

	protected static final String					TESTDATA_BASE_DIR		= "testdata";

	protected static final String					BUNDLES					= "bundles";

	protected static final String					STANDALONE				= "standalone";

	protected static final String					WORKSPACE				= "workspace";

	/* Test starts here */
	@Test
	public void test1() throws Exception {
		initTestData(WORKSPACE);
		folder.print(System.err, true);

		check(FileStatus.UNMODIFIED_EXISTS, "p", "workspace.bndrun");

		folder.print(System.err, true);

		// execBndCmd("run", "p/workspace.bndrun");
	}

	/* command execution */
	protected ByteArrayOutputStream[] execBndCmd(String... cmd) throws Exception {
		// TODO: impl.
		bnd.mainNoExit(cmd, folder.getRootPath());

		return null;
	}

	protected void initTestData(final String subdir) throws IOException {
		folder.copyDataFrom(Paths.get(TESTDATA_BASE_DIR, subdir))
			.snapshot(DEFAULT_SNAPSHOT_FUNC);
	}

	/* assertions */
	protected void check(FileStatus expectedFileStatus, String... p) {
		assertEquals(expectedFileStatus, folder.checkFile(Paths.get("", p)));
	}

	protected int countFilesWithStatus(final FileStatus fileStatus) {
		// TODO: impl.
		// final Map<Path, FileStatus> stat = folder.createFileStatistic(true);
		//
		// System.out.println(stat.values()
		// .stream()
		// .filter(s -> s == FileStatus.UNMODIFIED_EXISTS)
		// .count());

		return 0;
	}

}
