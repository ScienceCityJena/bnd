package aQute.bnd.main.testrules;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.rules.TemporaryFolder;

import aQute.lib.io.IO;

/**
 * The Class WatchedTemporaryFolder.
 */
public class WatchedTemporaryFolder extends TemporaryFolder implements WatchedFolder {

	private Map<Path, String>		snapshotData	= Collections.unmodifiableMap(new HashMap<>());

	private Function<Path, String>	snapshotFunc	= p -> "";

	@Override
	public Path getRootPath() {
		return getRoot().toPath();
	}

	@Override
	public WatchedFolder copyDataFrom(Path p) throws IOException {
		IO.copy(IO.getFile(p.toFile()
			.getAbsolutePath()), getRoot());

		return this;
	}

	@Override
	public void snapshot(Function<Path, String> func) throws IOException {
		snapshotFunc = func;
		snapshotData = Collections.unmodifiableMap(Files.walk(getRootPath())
			.collect(Collectors.toMap((Path p) -> p, p -> func.apply(p))));
	}

	@Override
	public WatchedFolder.FileStatus checkFile(Path relPath) {
		return computeFileStatus(getRootPath().resolve(relPath));
	}

	@Override
	public void print(PrintStream printStream, boolean relativize) throws IOException {
		Files.walk(getRootPath())
			.forEach(p -> printStream.println(relativize ? getRootPath().relativize(p) : p));
	}

	@Override
	public Map<Path, FileStatus> createFileStatistic(boolean relativize) throws IOException {
		return Files.walk(getRootPath())
			.collect(Collectors.toMap((Path p) -> (relativize ? getRootPath().relativize(p) : p),
				p -> computeFileStatus(p)));
	}

	private WatchedFolder.FileStatus computeFileStatus(final Path p) {
		final boolean snapshotFound = snapshotData.containsKey(p);

		if (Files.exists(p)) {
			if (!snapshotFound) {
				return FileStatus.CREATED;
			}

			return snapshotData.get(p)
				.equals(snapshotFunc.apply(p)) ? FileStatus.UNMODIFIED_EXISTS : FileStatus.MODIFIED;
		}

		return snapshotFound ? FileStatus.DELETED : FileStatus.UNMODIFIED_NOT_EXISTS;
	}
}
