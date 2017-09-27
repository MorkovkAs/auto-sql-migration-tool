package ru.morkovka.migrationspring.utils;

public class Queries {
	public static String q1 = ""
			+ "--table: schema.my_table"
			+ "--id: my_table_id"
			+ "UPDATE schema.my_table t SET some_field = 57\n"
			+ "WHERE t.my_table_id BETWEEN ? AND ?;";
}
