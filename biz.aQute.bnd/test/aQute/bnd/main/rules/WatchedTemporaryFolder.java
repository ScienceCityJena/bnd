package aQute.bnd.main.rules;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.rules.TemporaryFolder;

import aQute.lib.io.IO;

/**
 * The Class WatchedTemporaryFolder.
 */
public class WatchedTemporaryFolder extends TemporaryFolder implements WatchedFolder {

	private Map<Path, String>		snapshotData	= new TreeMap<>();

	private Function<Path, String>	snapshotFunc	= p -> "";

	@Override
	public void copyDataFrom(Path p) throws IOException {
		IO.copy(IO.getFile(p.toFile()
			.getAbsolutePath()), getRoot());
	}

	@Override
	public Path getRootPath() {
		return getRoot().toPath();
	}

	@Override
	public void snapshot(Function<Path, String> func) throws IOException {
		snapshotFunc = func;
		snapshotData = Files.walk(getRootPath())
			.collect(Collectors.toMap((Path p) -> p, p -> func.apply(p)));
	}

	@Override
	public WatchedFolder.FileStatus checkFile(Path relPath) {
		final Path p = Paths.get(getRoot().toString(), relPath.toString());

		final boolean snapshotFound = snapshotData.containsKey(p);

		if (Files.exists(p)) {
			if (!snapshotFound) {
				return FileStatus.CREATED;
			} else {
				final String snapValue = snapshotData.get(p);

				final String pValue = snapshotFunc.apply(p);

				return snapValue.equals(pValue) ? FileStatus.UNMODIFIED_EXISTS : FileStatus.MODIFIED;
			}
		}

		return snapshotFound ? FileStatus.DELETED : FileStatus.UNMODIFIED_NOT_EXISTS;
	}

	@Override
	public void print(PrintStream printStream, boolean relativize) throws IOException {
		final Path root = getRootPath();

		Files.walk(root, FileVisitOption.FOLLOW_LINKS)
			.forEach(p -> printStream.println(relativize ? root.relativize(p) : p));
	}
}
