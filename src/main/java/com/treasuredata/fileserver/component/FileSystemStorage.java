package com.treasuredata.fileserver.component;

import com.treasuredata.fileserver.component.exceptions.StorageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class FileSystemStorage {

    private String rootDirectory;

	private Path rootLocation;

	/**
	 * Get list files.
	 */
	public Stream<Path> loadAll() {
		try {
			return Files.walk(this.rootLocation, 1)
					.filter(path -> !path.equals(this.rootLocation));
		} catch (IOException e) {
			throw new StorageException("Failed to read files of directory: " + rootDirectory, e);
		}
	}

	/**
	 * Get file info.
	 */
	public Path load(String filename) {
		return rootLocation.resolve(filename);
	}

	/**
	 * Init directory.
	 */
	public void setRootLocation(String dir) {
		try {
            rootDirectory = dir;
			rootLocation = Paths.get(rootDirectory);
			if (!Files.exists(rootLocation)) {
				throw new StorageException("Directory does not exits.");
			}
		} catch (Exception e) {
			throw new StorageException(e.getMessage(), e);
		}
	}
}
