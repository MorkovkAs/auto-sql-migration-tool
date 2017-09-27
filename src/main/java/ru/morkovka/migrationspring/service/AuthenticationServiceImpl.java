package ru.morkovka.migrationspring.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

	private static List<String> keys = new ArrayList<>();

	@Override
	public boolean checkKey(String key) {
		return keys != null && keys.contains(key);
	}

	@Override
	public boolean deleteKey(String key) {
		return key != null && keys.remove(key);
	}

	@Override
	public boolean checkAndDeleteKeyIfExist(String key) {
		return checkKey(key) && deleteKey(key);
	}

	@Override
	public List<String> generateNewKeys(int n) {
		keys.clear();
		Random random = new Random();
		for (int i = 0; i < n; i++) {
			keys.add("" + random.nextInt());
		}
		printKeys();
		return keys;
	}

	private void printKeys() {
		keys.forEach(key -> System.out.print(key + ", "));
	}
}
