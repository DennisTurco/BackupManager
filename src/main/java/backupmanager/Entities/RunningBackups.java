package backupmanager.Entities;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import backupmanager.BackupOperations;
import backupmanager.Logger;
import backupmanager.Logger.LogLevel;
import backupmanager.Enums.ConfigKey;

// this class contains only the RunningBackups entity
// this entity is used to store the information of the backups that are currently running
// i use this object to know wich backups are currently running across the instances
public class RunningBackups {
    private String backupName;
    private String path;
    private int progress;

    public RunningBackups(String backupName, String path, int progress) {
        this.backupName = backupName;
        this.path = path;
        this.progress = progress;
    }

    public static List<RunningBackups> readBackupListFromJSON() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(ConfigKey.CONFIG_DIRECTORY_STRING.getValue() + ConfigKey.RUNNING_BACKUPS_FILE_STRING.getValue())) {
            // Check if the JSON starts with an array, and if not, try to fix the file
            String fileContent = new String(Files.readAllBytes(Paths.get(ConfigKey.CONFIG_DIRECTORY_STRING.getValue() + ConfigKey.RUNNING_BACKUPS_FILE_STRING.getValue())), StandardCharsets.UTF_8);
            
            // Try to parse the JSON string into a valid list of objects
            if (!fileContent.trim().startsWith("[")) {
                System.out.println("Malformed JSON file. Attempting to fix...");
                // Attempt to fix the malformed JSON file
                fileContent = "[" + fileContent.replaceAll("(?<=})\\s*(?=\\{)", ",") + "]";
                Files.write(Paths.get(ConfigKey.CONFIG_DIRECTORY_STRING.getValue() + ConfigKey.RUNNING_BACKUPS_FILE_STRING.getValue()), fileContent.getBytes());
            }

            Type listType = new TypeToken<ArrayList<RunningBackups>>() {}.getType();
            List<RunningBackups> backups = gson.fromJson(fileContent, listType);
            
            return backups != null ? backups : new ArrayList<>();
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } catch (JsonSyntaxException e) {
            System.out.println("Malformed JSON in file: " + e.getMessage());
            e.printStackTrace();
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
        boolean updated = false;
    
        // Iterate through existing backups to update
        for (int i = 0; i < backups.size(); i++) {
            if (backups.get(i).getBackupName().equals(backup.getBackupName())) {
                backups.set(i, backup);  // Update backup
                updated = true;
                break;
            }
        }
    
        // If backup doesn't exist, add it
        if (!updated) {
            backups.add(backup);  // Add new backup to list
        }
    
        // If progress is 100, delete the backup from the list before updating the file
        if (backup != null && backup.getProgress() == 100) {
            deleteBackupFromJSON(backup.getBackupName());
        } else {
            // Only update the file if the backup was added or modified
            updateBackupsToJSON(backups);
        }
    }
    
    public static void updateBackupsToJSON(List<RunningBackups> backups) {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(ConfigKey.CONFIG_DIRECTORY_STRING.getValue() + ConfigKey.RUNNING_BACKUPS_FILE_STRING.getValue())) {
            // Ensure JSON is written properly by serializing the list
            gson.toJson(backups, writer);
            writer.flush();  // Make sure the data is written to the file
        } catch (IOException e) {
            System.out.println("Error writing to JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void cleanRunningBackupsFromJSON(String backupName) {
        List<RunningBackups> backups = readBackupListFromJSON();
    
        // Use an Iterator to safely remove items while iterating
        Iterator<RunningBackups> iterator = backups.iterator();
        while (iterator.hasNext()) {
            RunningBackups runningBackup = iterator.next();
            
            Logger.logMessage("Deleting partial backup: " + runningBackup.getPath(), LogLevel.INFO);

            if (runningBackup.getProgress() != 100 && BackupOperations.deletePartialBackup(runningBackup.getPath())) {
                iterator.remove();
            } else if (runningBackup.getProgress() == 100 && runningBackup.getBackupName().equals(backupName)) {
                iterator.remove();
            }
        }
    
        updateBackupsToJSON(backups);
    }

    public static void deleteBackupFromJSON(String backupName) {
        List<RunningBackups> backups = readBackupListFromJSON();
        if (backups != null) {
            backups.removeIf(backup -> backup.getBackupName().equals(backupName));
            updateBackupsToJSON(backups); // Update the JSON file after deletion
        }
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

    public void setBackupName(String backupName) {
        this.backupName = backupName;
    }
    public void setProgress(int progress) {
        this.progress = progress;
    }
    public void setPath(String path) {
        this.path = path;
    }
}   
