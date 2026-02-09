UPDATE Configurations SET Value = 'True' WHERE Code = 'SubscriptionNedded';

-- One month of free subscription
INSERT INTO Subscriptions (InsertDate, StartDate, EndDate, CreatedBy)
SELECT
    unixepoch() * 1000,
    unixepoch() * 1000,
    unixepoch('now', '+1 month') * 1000,
    'MIGRATION';