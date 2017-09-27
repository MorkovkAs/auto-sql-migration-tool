package ru.morkovka.migrationspring.utils;

import com.google.common.base.Strings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReadingUtils {

	private static List<String> findScripts(int sprint) {
		List<String> scripts = new ArrayList<>();
		File folder = new File("../mvd-soop/soop-db/sql/todo");

		File[] files = folder.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.getName().startsWith("V" + sprint + "_") && file.getName().endsWith("_MIGRATION.sql")) {
					scripts.add(file.getPath());
					System.out.println(file.getName() + "\n");
				}
			}
		}

		return scripts;
	}

	private static String readFile(String path) {
		try {
			return Files.readAllLines(Paths.get(path)).stream()
					.filter(Objects::nonNull)
					.map(String::trim)
					.filter((String s) -> !s.startsWith("--") || s.startsWith("--table: ") || s.startsWith("--id: "))
					.filter(s -> !Strings.isNullOrEmpty(s))
					.collect(Collectors.joining("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	private static List<String> splitToQueries(String script) {
		return Arrays.asList(script.split(";"));
	}

	private static List<String> parseQuery(String query) {
		if (Strings.isNullOrEmpty(query)) {
			return null;
		}
		String table = query.substring(query.indexOf("--table: ") + 9, query.indexOf("--id: ") - 1);
		String id = query.substring(query.indexOf("--id: ") + 6, query.indexOf("\n", query.indexOf("--id: ")));

		return Arrays.asList(query, table, id);
	}

	public static List<List<String>> getQueriesInfoBySprint(int sprint) {
		List<String> queries = new ArrayList<>();
		List<List<String>> queriesInfo = new ArrayList<>();
		findScripts(sprint).stream()
				.filter(s -> !Strings.isNullOrEmpty(s))
				.map(String::trim)
				.forEach(path -> queries.addAll(splitToQueries(readFile(path))));
		queries.forEach(q -> queriesInfo.add(parseQuery(q)));

		return queriesInfo;
	}
}
