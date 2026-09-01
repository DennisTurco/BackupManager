package backupmanager.Entities;

public record ZippingContext (
    BackupExecutionContext execution,
    BackupUIContext ui
) { }
