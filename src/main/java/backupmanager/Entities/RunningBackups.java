package backupmanager.Entities;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import backupmanager.BackupOperations;
import backupmanager.Enums.ConfigKey;
import backupmanager.Logger;
import backupmanager.Logger.LogLevel;

// this class contains only the RunningBackups entity
// this entity is used to store the information of the backups that are currently running
// i use this object to know wich backups are currently running across the instances
public class RunningBackups {
    private final String backupName;
    private final String path;
    private final int progress;

    public RunningBackups(String backupName, String path, int progress) {
        this.backupName = backupName;
        this.path = path;
        this.progress = progress;
    }

    public static List<RunningBackups> readBackupListFromJSON() {
        Gson gson = new Gson();
        Path filePath = Paths.get(ConfigKey.CONFIG_DIRECTORY_STRING.getValue() + ConfigKey.RUNNING_BACKUPS_FILE_STRING.getValue());
        
        try {
            // Check if the file exists, otherwise create it with an empty array
            if (!Files.exists(filePath)) {
                Logger.logMessage("Backup file not found. Creating a new empty file...", LogLevel.INFO);
                Files.write(filePath, "[]".getBytes(), StandardOpenOption.CREATE_NEW);
            }

            // Read the content of the file
            String fileContent = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            
            // Try to parse the JSON string into a valid list of objects
            if (!fileContent.trim().startsWith("[")) {
                Logger.logMessage("Malformed JSON file. Attempting to fix...", LogLevel.WARN);
                // Attempt to fix the malformed JSON file
                fileContent = "[" + fileContent.replaceAll("(?<=})\\s*(?=\\{)", ",") + "]";
                Files.write(filePath, fileContent.getBytes());
            }

            // Deserialize the JSON into the list of RunningBackups objects
            Type listType = new TypeToken<ArrayList<RunningBackups>>() {}.getType();
            List<RunningBackups> backups = gson.fromJson(fileContent, listType);
            
            return backups != null ? backups : new ArrayList<>();
            
        } catch (IOException e) {
            Logger.logMessage("Error reading file: " + e.getMessage(), LogLevel.ERROR, e);
            return new ArrayList<>();
        } catch (JsonSyntaxException e) {
            Logger.logMessage("Malformed JSON in file: " + e.getMessage(), LogLevel.ERROR, e);
            return new ArrayList<>();
        }
    }

    public static RunningBackups readBackupFromJSON(String backupName) {
        List<RunningBackups> backups = readBackupListFromJSON();
        for (RunningBackups backup : backups) {
            if (backup.getBackupName().equals(backupName)) {
                return backup;
            }
        }
        return null; // Return null if no backup with the specified name is found
    }

    public static void updateBackupToJSON(RunningBackups backup) {
        List<RunningBackups> backups = readBackupListFromJSON();
    
        ListIterator<RunningBackups> iterator = backups.listIterator();
        boolean updated = false;
    
        // Iterate through existing backups to update
        while (iterator.hasNext()) {
            RunningBackups currentBackup = iterator.next();
            if (currentBackup.getBackupName().equals(backup.getBackupName())) {
                if (backup.getProgress() == 100) {
                    iterator.remove();
                } else {
                    iterator.set(backup);
                }
                updated = true;
                break;
            }
        }
    
        // If the backup wasn't found in the list, add it
        if (!updated && backup.getProgress() != 100) {
            backups.add(backup);
        }
    
        // Write the updated backups to JSON
        updateBackupsToJSON(backups);
    }
    
    public static void updateBackupsToJSON(List<RunningBackups> backups) {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(ConfigKey.CONFIG_DIRECTORY_STRING.getValue() + ConfigKey.RUNNING_BACKUPS_FILE_STRING.getValue())) {
            // Ensure JSON is written properly by serializing the list
            gson.toJson(backups, writer);
            writer.flush();  // Make sure the data is written to the file
        } catch (IOException e) {
            Logger.logMessage("Error writing to JSON file: " + e.getMessage(), LogLevel.ERROR, e);
        }
    }

    public static void cleanRunningBackupsFromJSON(String backupName) {
        List<RunningBackups> backups = readBackupListFromJSON();
    
        // Use an Iterator to safely remove items while iterating
        Iterator<RunningBackups> iterator = backups.iterator();
        while (iterator.hasNext()) {
            RunningBackups runningBackup = iterator.next();
            
            Logger.logMessage("Deleting partial backup: " + runningBackup.getPath(), LogLevel.INFO);

            if ((runningBackup.getProgress() != 100 && BackupOperations.deletePartialBackup(runningBackup.getPath()))
                    || (runningBackup.getProgress() == 100 && runningBackup.getBackupName().equals(backupName))) {
                iterator.remove();
            }
        }
    
        updateBackupsToJSON(backups);
    }
    
    public static void deletePartialBackupsStuckedJSONFile() {
        List<RunningBackups> backups = readBackupListFromJSON();
    
        // Use an iterator to safely remove items while iterating
        Iterator<RunningBackups> iterator = backups.iterator();
        while (iterator.hasNext()) {
            RunningBackups backup = iterator.next();
            
            // Call the delete method if necessary
            if (BackupOperations.deletePartialBackup(backup.getPath())) {
                // If you need to remove the backup from the list after deletion, you can do so safely
                iterator.remove(); // This ensures no ConcurrentModificationException occurs
            }
        }
    
        updateBackupsToJSON(backups);
    }

    public String getBackupName() {
        return backupName;
    }
    public int getProgress() {
        return progress;
    }
    public String getPath() {
        return path;
    }
}   
