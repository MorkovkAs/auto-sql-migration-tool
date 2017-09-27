package ru.morkovka.migrationspring.service;

public interface MigrationService {

	boolean validatePermissionAndStartMigration(String key);
}
