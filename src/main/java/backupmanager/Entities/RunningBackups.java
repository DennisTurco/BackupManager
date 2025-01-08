package backupmanager.Entities;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import backupmanager.Enums.ConfigKey;

// this class contains only the RunningBackups entity
// this entity is used to store the information of the backups that are currently running
// i use this object to know wich backups are currently running across the instances
public class RunningBackups extends Backup {
    private float progress;

    public RunningBackups(Backup backup, float progress) {
        super(backup);
        this.progress = progress;
    }

    public static List<RunningBackups> readBackupListFromJSON() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(ConfigKey.CONFIG_DIRECTORY_STRING.getValue() + ConfigKey.RUNNING_BACKUPS_FILE_STRING.getValue())) {
            Type listType = new TypeToken<ArrayList<RunningBackups>>() {}.getType();
            List<RunningBackups> backups = gson.fromJson(reader, listType);
            return backups != null ? backups : new ArrayList<>(); // Ensure backups is not null
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>(); // Return an empty list if the file doesn't exist or an error occurs
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

        for (int i = 0; i < backups.size(); i++) {
            if (backups.get(i).getBackupName().equals(backup.getBackupName())) {
                backups.set(i, backup);
                updated = true;
                break;
            }
        }

        if (!updated) {
            backups.add(backup); // If the backup doesn't exist, add it to the list
        }

        if (backup != null && backup.getProgress() == 100) {
            deleteBackupFromJSON(backup.getBackupName());
        } else {
            updateBackupsToJSON(backups);
        }
    }

    public static void updateBackupsToJSON(List<RunningBackups> backups) {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(ConfigKey.CONFIG_DIRECTORY_STRING.getValue() + ConfigKey.RUNNING_BACKUPS_FILE_STRING.getValue())) {
            gson.toJson(backups, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteBackupFromJSON(String backupName) {
        List<RunningBackups> backups = readBackupListFromJSON();
        if (backups != null) {
            backups.removeIf(backup -> backup.getBackupName().equals(backupName));
            updateBackupsToJSON(backups); // Update the JSON file after deletion
        }
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }
}   
