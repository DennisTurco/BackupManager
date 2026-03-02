package backupmanager.gui.sample.csv;

import java.util.ArrayList;
import java.util.List;

import backupmanager.Entities.ConfigurationBackup;

public class ConfigurationBackupDataTable extends ResponsePageable<List<ConfigurationBackup>> {
    public ConfigurationBackupDataTable(int total, int page, int pageSize, int limit, List<ConfigurationBackup> data) {
        super(total, page, pageSize, limit, data);
    }

    public static ConfigurationBackupDataTable create(List<ConfigurationBackup> rows, int page, int limit) {
        int total = rows.size();
        int pageSize = (int) Math.ceil((double) total / limit);

        if (page > pageSize) {
            page = pageSize;
        }

        int start = Math.min((page - 1) * limit, total);
        int end = Math.min(start + limit, total);
        return new ConfigurationBackupDataTable(total, page, pageSize, limit, new ArrayList<>(rows.subList(start, end)));
    }
}
