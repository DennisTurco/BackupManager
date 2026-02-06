package backupmanager.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderUtils {

    private static final Logger logger = LoggerFactory.getLogger(FolderUtils.class);

    public static long calculateFileOrFolderSize(String pathStr) {
        Path path = Path.of(pathStr);
        if (Files.isDirectory(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                return walk
                        .filter(Files::isRegularFile)
                        .mapToLong(p -> {
                            try {
                                return Files.size(p);
                            } catch (IOException e) {
                                return 0L;
                            }
                        })
                        .sum();
            } catch (IOException e) {
                throw new RuntimeException("Error calculating folder size", e);
            }
        } else if (Files.isRegularFile(path)) {
            try {
                return Files.size(path);
            } catch (IOException e) {
                throw new RuntimeException("Error calculating file size", e);
            }
        } else {
            throw new IllegalArgumentException("Path does not exist: " + pathStr);
        }
    }


    public static int countFilesInDirectory(File directory) {
        if (directory == null) {
            logger.warn("Directory is null");
            return -1;
        }
        if (!directory.canRead()) {
            logger.warn("Unable to read directory: " + directory.getAbsolutePath());
            return -1;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            logger.warn("Unable to list files for directory: " + directory.getAbsolutePath());
            return -1;
        }

    	int count = 0;
        for (File file : files) {
            if (file.isFile()) {
                count++;
            } else if (file.isDirectory()) {
                count += countFilesInDirectory(file); // Recursively count files in subdirectories.
            }
        }
        return count;
    }
}
