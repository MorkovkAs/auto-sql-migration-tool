package ru.morkovka.migrationspring.service;

import java.util.List;

public interface AuthenticationService {

	boolean checkKey(String key);

	boolean deleteKey(String key);

	boolean checkAndDeleteKeyIfExist(String key);

	List<String> generateNewKeys(int n);
}
