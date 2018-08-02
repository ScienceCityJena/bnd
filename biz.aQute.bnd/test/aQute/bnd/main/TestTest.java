package aQute.bnd.main;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import org.junit.Rule;
import org.junit.Test;

import aQute.bnd.main.rules.WatchedFolder;
import aQute.bnd.main.rules.WatchedTemporaryFolder;

public class TestTest {

	@Rule
	public WatchedFolder						folder				= new WatchedTemporaryFolder();

	protected static final Function<Path, String>	FUNC_FILE_TSTAMP	= p -> String.valueOf(p.toFile()
		.lastModified());

	/////////////////////

	@Test
	public void test1() throws Exception {
		folder.copyDataFrom(Paths.get("testdata", "workspace"));

		folder.snapshot(FUNC_FILE_TSTAMP);

		assertEquals(folder.checkFile(Paths.get("p", ".gitignore")), WatchedFolder.FileStatus.UNMODIFIED_EXISTS);

		execBndCmd("run", "p/workspace.bndrun");
	}

	public ByteArrayOutputStream[] execBndCmd(String... cmd) throws Exception {
		bnd.mainNoExit(cmd, folder.getRootPath());
		return null;
	}

	////////////

}
