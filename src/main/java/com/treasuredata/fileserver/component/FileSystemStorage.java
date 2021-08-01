package com.treasuredata.fileserver.component;

import com.treasuredata.fileserver.component.exceptions.StorageFileNotFoundException;
import com.treasuredata.fileserver.component.exceptions.StorageNotExistException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FileSystemStorage {

	private Path rootLocation;

	/**
	 * Get list text files.
	 */
	public List<Path> loadAll() throws IOException {
        if (rootLocation == null) {
            throw new StorageFileNotFoundException("Directory does not exist.");
        }
        return Files.walk(rootLocation, 1)
                .filter(path -> !path.equals(rootLocation) && path.toFile().getName().endsWith(".txt"))
                .collect(Collectors.toList());
	}

	/**
	 * Get file info.
	 */
	public Path load(String filename) {
	    if (rootLocation == null) {
	        throw new StorageFileNotFoundException("File " + filename + " does not exist.");
        }
		return rootLocation.resolve(filename);
	}

	/**
	 * Init directory.
	 */
	public void setRootLocation(String dir) {
        rootLocation = Paths.get(dir);
        if (!Files.exists(rootLocation)) {
            rootLocation = null;
            throw new StorageNotExistException("Directory does not exist.");
        }
	}
}
