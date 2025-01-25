package backupmanager.Json;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import backupmanager.Entities.Backup;
import backupmanager.Entities.Preferences;
import backupmanager.Entities.TimeInterval;
import backupmanager.Interfaces.IJSONBackup;
import backupmanager.Managers.ExceptionManager;

public class JSONBackup implements IJSONBackup {
    
    private static final Logger logger = LoggerFactory.getLogger(JSONBackup.class);

    @Override
    public List<Backup> readBackupListFromJSON(String directoryPath, String filename) throws IOException {
        List<Backup> backupList = new ArrayList<>();
    
        // Check if the directory is correct, otherwise reset to default
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            logger.warn("Directory of the backup list file doesn't exist (" + directoryPath + "), reset to default value.");
            Preferences.setBackupList(Preferences.getDefaultBackupList());
            Preferences.updatePreferencesToJSON();
            directoryPath = Preferences.getBackupList().getDirectory();
        }
    
        String filePath = directoryPath + filename;
        File file = new File(filePath);
    
        // Check if the file exists and is not empty
        if (!file.exists()) {
            file.createNewFile();
            logger.info("New backup list created with name: " + filePath);
        }
        if (file.length() == 0) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("[]");
                logger.info("File initialized with empty JSON array: []");
            } catch (IOException e) {
                logger.error("Error initializing file: " + e.getMessage(), e);
                throw e;
            }
        }
    
        try (Reader reader = new FileReader(filePath)) {
            JsonArray backupArray = JsonParser.parseReader(reader).getAsJsonArray();
    
            for (JsonElement element : backupArray) {
                JsonObject backupObj = element.getAsJsonObject();
    
                String backupNameValue = getStringOrNull(backupObj, "backup_name");
                String startPathValue = getStringOrNull(backupObj, "start_path");
                String destinationPathValue = getStringOrNull(backupObj, "destination_path");
                String lastBackupStr = getStringOrNull(backupObj, "last_backup");
                String notesValue = getStringOrNull(backupObj, "notes");
                String creationDateStr = getStringOrNull(backupObj, "creation_date");
                String lastUpdateDateStr = getStringOrNull(backupObj, "last_update_date");
                int backupCountValue = backupObj.has("backup_count") ? backupObj.get("backup_count").getAsInt() : 0;
                int maxBackupsToKeepValue = backupObj.has("max_backups_to_keep") ? backupObj.get("max_backups_to_keep").getAsInt() : 0;
    
                Boolean automaticBackupValue = backupObj.has("automatic_backup") && !backupObj.get("automatic_backup").isJsonNull() 
                    ? backupObj.get("automatic_backup").getAsBoolean() 
                    : null;
    
                String nextDateBackupStr = getStringOrNull(backupObj, "next_date_backup");
                String daysIntervalBackupStr = getStringOrNull(backupObj, "time_interval_backup");
    
                LocalDateTime lastBackupValue = lastBackupStr != null ? LocalDateTime.parse(lastBackupStr) : null;
                LocalDateTime nextDateBackupValue = nextDateBackupStr != null ? LocalDateTime.parse(nextDateBackupStr) : null;
                LocalDateTime creationDateValue = creationDateStr != null ? LocalDateTime.parse(creationDateStr) : null;
                LocalDateTime lastUpdateDateValue = lastUpdateDateStr != null ? LocalDateTime.parse(lastUpdateDateStr) : null;
    
                backupList.add(new Backup(
                    backupNameValue,
                    startPathValue,
                    destinationPathValue,
                    lastBackupValue,
                    automaticBackupValue,
                    nextDateBackupValue,
                    TimeInterval.getTimeIntervalFromString(daysIntervalBackupStr),
                    notesValue,
                    creationDateValue,
                    lastUpdateDateValue,
                    backupCountValue,
                    maxBackupsToKeepValue
                ));
            }
    
        } catch (IOException | JsonSyntaxException | NullPointerException ex) {
            logger.error("An error occurred: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
        return backupList;
    }
    
    // Helper method to safely retrieve a string or null
    private String getStringOrNull(JsonObject obj, String property) {
        return obj.has(property) && !obj.get(property).isJsonNull() ? obj.get(property).getAsString() : null;
    }
    
    @Override
    public void updateBackupListJSON(String directoryPath, String filename, List<Backup> backups) {
        String filePath = directoryPath + filename;

        try (Writer writer = new FileWriter(filePath)) {
            // Use Gson to convert the list of backups into a JSON array
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonArray updatedBackupArray = new JsonArray();

            for (Backup backup : backups) {
                JsonObject backupObject = new JsonObject();
                backupObject.addProperty("backup_name", backup.getBackupName());
                backupObject.addProperty("start_path", backup.getInitialPath());
                backupObject.addProperty("destination_path", backup.getDestinationPath());
                backupObject.addProperty("last_backup", backup.getLastBackup() != null ? backup.getLastBackup().toString() : null);
                backupObject.addProperty("automatic_backup", backup.isAutoBackup());
                backupObject.addProperty("next_date_backup", backup.getNextDateBackup() != null ? backup.getNextDateBackup().toString() : null);
                backupObject.addProperty("time_interval_backup", backup.getTimeIntervalBackup() != null ? backup.getTimeIntervalBackup().toString() : null);
                backupObject.addProperty("notes", backup.getNotes());
                backupObject.addProperty("creation_date", backup.getCreationDate() != null ? backup.getCreationDate().toString() : null);
                backupObject.addProperty("last_update_date", backup.getLastUpdateDate() != null ? backup.getLastUpdateDate().toString() : null);
                backupObject.addProperty("backup_count", backup.getBackupCount());
                backupObject.addProperty("max_backups_to_keep", backup.getMaxBackupsToKeep());

                updatedBackupArray.add(backupObject);
            }

            // Write the JSON array to the file
            gson.toJson(updatedBackupArray, writer);
        } catch (IOException ex) {
            logger.error("An error occurred: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }
    
    @Override
    public void updateSingleBackupInJSON(String directoryPath, String filename, Backup updatedBackup) {
        String filePath = directoryPath + filename;

        try (Reader reader = new FileReader(filePath)) {
            // Parse JSON file into a list of Backup objects using Gson
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Type listType = new TypeToken<List<JsonObject>>() {}.getType();
            List<JsonObject> backupList = gson.fromJson(reader, listType);

            // Find and update the specific backup
            for (JsonObject backupObject : backupList) {
                String backupName = backupObject.get("backup_name").getAsString();
                if (backupName.equals(updatedBackup.getBackupName())) {
                    backupObject.addProperty("start_path", updatedBackup.getInitialPath());
                    backupObject.addProperty("destination_path", updatedBackup.getDestinationPath());
                    backupObject.addProperty("last_backup", updatedBackup.getLastBackup() != null ? updatedBackup.getLastBackup().toString() : null);
                    backupObject.addProperty("automatic_backup", updatedBackup.isAutoBackup());
                    backupObject.addProperty("next_date_backup", updatedBackup.getNextDateBackup() != null ? updatedBackup.getNextDateBackup().toString() : null);
                    backupObject.addProperty("time_interval_backup", updatedBackup.getTimeIntervalBackup() != null ? updatedBackup.getTimeIntervalBackup().toString() : null);
                    backupObject.addProperty("notes", updatedBackup.getNotes());
                    backupObject.addProperty("creation_date", updatedBackup.getCreationDate() != null ? updatedBackup.getCreationDate().toString() : null);
                    backupObject.addProperty("last_update_date", updatedBackup.getLastUpdateDate() != null ? updatedBackup.getLastUpdateDate().toString() : null);
                    backupObject.addProperty("backup_count", updatedBackup.getBackupCount());
                    backupObject.addProperty("max_backups_to_keep", updatedBackup.getMaxBackupsToKeep());
                    break;
                }
            }

            // Write updated list back to the JSON file
            try (Writer writer = new FileWriter(filePath)) {
                gson.toJson(backupList, writer);
            } catch (IOException ex) {
                logger.error("An error occurred: " + ex.getMessage(),  ex);
                ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
            }

        } catch (IOException ex) {
            logger.error("An error occurred: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        } catch (JsonSyntaxException ex) {
            logger.error("Invalid JSON format: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }
}