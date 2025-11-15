package com.example.backend.service;

public interface DataMigrationService {
    void migrateAllStagingToActual(String processInstanceId);
    void migrateProducts(String sheetId);
    void migratePlans(String sheetId);
    void migrateItems(String sheetId);
}

