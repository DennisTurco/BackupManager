package backupmanager.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FolderUtils {

    public static long calculateFolderSize(String folder) {
        try (Stream<Path> walk = Files.walk(Path.of(folder))) {

            return walk
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();

        } catch (IOException e) {
            throw new RuntimeException("Error calculating folder size", e);
        }
    }
}
