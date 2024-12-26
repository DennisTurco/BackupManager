package backupmanager.Json;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static backupmanager.GUI.BackupManagerGUI.OpenExceptionMessage;
import backupmanager.Entities.Backup;
import backupmanager.Entities.Preferences;
import backupmanager.Entities.TimeInterval;
import backupmanager.Interfaces.IJSONAutoBackup;
import backupmanager.Logger;
import backupmanager.Logger.LogLevel;

public class JSONAutoBackup implements IJSONAutoBackup {
    @Override
    public List<Backup> ReadBackupListFromJSON(String directoryPath, String filename) throws IOException {
        List<Backup> backupList = new ArrayList<>();
        
        // check if the directory is correct, otherwise we have to reset to default
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            Logger.logMessage("Directory of the backup list file doesn't exists (" + directoryPath + "), resetted to default value.", LogLevel.INFO);
            Preferences.setBackupList(Preferences.getDefaultBackupList());
            Preferences.updatePreferencesToJSON();
            directoryPath = Preferences.getBackupList().getDirectory();
        }

        String filePath = directoryPath + filename;
        File file = new File(filePath);

        // Check if the file exists and is not empty
        if (!file.exists()) {
            file.createNewFile();
            Logger.logMessage("New backup list created with name: " + filePath, LogLevel.INFO);
        }
        if (file.length() == 0) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("[]");
                Logger.logMessage("File initialized with empty JSON array: []", LogLevel.INFO);
            } catch (IOException e) {
                Logger.logMessage("Error initializing file: " + e.getMessage(), LogLevel.ERROR, e);
                throw e;
            }
        }

        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(filePath)) {
            JSONArray backupArray = (JSONArray) parser.parse(reader);

            for (Object obj : backupArray) {
                JSONObject backupObj = (JSONObject) obj;

                String backupNameValue = (String) backupObj.get("backup_name");
                String startPathValue = (String) backupObj.get("start_path");
                String destinationPathValue = (String) backupObj.get("destination_path");
                String lastBackupStr = (String) backupObj.get("last_backup");
                String notesValue = (String) backupObj.get("notes");
                String creationDateStr = (String) backupObj.get("creation_date");
                String lastUpdateDateStr = (String) backupObj.get("last_update_date");
                int backupCountValue = Math.toIntExact((Long) backupObj.get("backup_count"));
                int maxBackupsToKeepValue = Math.toIntExact((Long) backupObj.get("max_backups_to_keep"));

                Object value = backupObj.get("automatic_backup");
                Boolean automaticBackupValue = null;
                if (value instanceof Boolean aBoolean) {
                    automaticBackupValue = aBoolean;
                } else if (value instanceof String string) {
                    automaticBackupValue = Boolean.valueOf(string);
                } else if (value instanceof Integer integer) {
                    automaticBackupValue = (integer == 1);
                }
                String nextDateBackupStr = (String) backupObj.get("next_date_backup");
                String daysIntervalBackupStr = (String) backupObj.get("time_interval_backup");

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

        } catch (IOException | ParseException | NullPointerException ex) {
            Logger.logMessage("An error occurred: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            OpenExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
        return backupList;
    }
    
    @Override
    public void UpdateBackupListJSON(String directoryPath, String filename, List<Backup> backups) {
        String filePath = directoryPath + filename;
        
        JSONArray updatedBackupArray = new JSONArray();
        for (Backup backup : backups) {
            JSONObject backupObject = new JSONObject();
            backupObject.put("backup_name", backup.getBackupName());
            backupObject.put("start_path", backup.getInitialPath());
            backupObject.put("destination_path", backup.getDestinationPath());
            backupObject.put("last_backup", backup.getLastBackup() != null ? backup.getLastBackup().toString() : null);
            backupObject.put("automatic_backup", backup.isAutoBackup());
            backupObject.put("next_date_backup", backup.getNextDateBackup() != null ? backup.getNextDateBackup().toString() : null);
            backupObject.put("time_interval_backup", backup.getTimeIntervalBackup() != null ? backup.getTimeIntervalBackup().toString() : null);
            backupObject.put("notes", backup.getNotes());
            backupObject.put("creation_date", backup.getCreationDate() != null ? backup.getCreationDate().toString() : null);
            backupObject.put("last_update_date", backup.getLastUpdateDate() != null ? backup.getLastUpdateDate().toString() : null);
            backupObject.put("backup_count", backup.getBackupCount());
            backupObject.put("max_backups_to_keep", backup.getMaxBackupsToKeep());

            updatedBackupArray.add(backupObject);
        }

        try (FileWriter file = new FileWriter(filePath)) {
            file.write(updatedBackupArray.toJSONString());
            file.flush();
        } catch (IOException ex) {
            Logger.logMessage("An error occurred: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            OpenExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }
    
    @Override
    public void UpdateSingleBackupInJSON(String directoryPath, String filename, Backup updatedBackup) {
        String filePath = directoryPath + filename;

        try (FileReader reader = new FileReader(filePath)) {
            JSONParser jsonParser = new JSONParser();
            JSONArray backupArray = (JSONArray) jsonParser.parse(reader);

            for (Object obj : backupArray) {
                JSONObject backupObject = (JSONObject) obj;

                String backupName = (String) backupObject.get("backup_name");
                if (backupName.equals(updatedBackup.getBackupName())) {
                    backupObject.put("start_path", updatedBackup.getInitialPath());
                    backupObject.put("destination_path", updatedBackup.getDestinationPath());
                    backupObject.put("last_backup", updatedBackup.getLastBackup() != null ? updatedBackup.getLastBackup().toString() : null);
                    backupObject.put("automatic_backup", updatedBackup.isAutoBackup());
                    backupObject.put("next_date_backup", updatedBackup.getNextDateBackup() != null ? updatedBackup.getNextDateBackup().toString() : null);
                    backupObject.put("time_interval_backup", updatedBackup.getTimeIntervalBackup() != null ? updatedBackup.getTimeIntervalBackup().toString() : null);
                    backupObject.put("notes", updatedBackup.getNotes());
                    backupObject.put("creation_date", updatedBackup.getCreationDate() != null ? updatedBackup.getCreationDate().toString() : null);
                    backupObject.put("last_update_date", updatedBackup.getLastUpdateDate() != null ? updatedBackup.getLastUpdateDate().toString() : null);
                    backupObject.put("backup_count", updatedBackup.getBackupCount());
                    backupObject.put("max_backups_to_keep", updatedBackup.getMaxBackupsToKeep());
                    break;
                }
            }

            try (FileWriter file = new FileWriter(filePath)) {
                file.write(backupArray.toJSONString());
                file.flush();
            } catch (IOException ex) {
                OpenExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
            }

        } catch (IOException | ParseException ex) {
            Logger.logMessage("An error occurred: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            OpenExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }
}