package aQute.bnd.main.rules;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.function.Function;

import org.junit.rules.TestRule;

public interface WatchedFolder extends TestRule {

	/**
	 * Copy data from source to watched folder.
	 *
	 * @param srcDir the source directory
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void copyDataFrom(Path srcDir) throws IOException;

	/**
	 * Takes a snapshot of the actual content of the folder.
	 *
	 * @param func the function applied to a path to recognice changes of the
	 *            underlying file.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void snapshot(Function<Path, String> func) throws IOException;

	/**
	 * Check file against the snapshot if exists.
	 *
	 * @param relPath the releative path to the root of the watched folder.
	 * @return the file status
	 */
	FileStatus checkFile(Path relPath);

	/**
	 * Prints the content of the folder.
	 *
	 * @param printStream the printstream
	 * @param relativize the iff true print pathes relative to folder's root
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void print(PrintStream printStream, boolean relativize) throws IOException;

	/**
	 * Gets the root path of the folder.
	 *
	 * @return the root path of the folder
	 */
	Path getRootPath();

	/**
	 * The Enum FileStatus.
	 */
	public enum FileStatus {
		// File exists and is not part of snapshot.
		CREATED,

		// File exists and is part of snapshot with different value.
		MODIFIED,

		// File exists and is part of snapshot with same value.
		UNMODIFIED_EXISTS,

		// File does not exists, but is part of snapshot.
		DELETED,

		// File does not exists and is not part of snapshot.
		UNMODIFIED_NOT_EXISTS;
	}
}
