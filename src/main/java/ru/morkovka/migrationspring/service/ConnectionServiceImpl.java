package ru.morkovka.migrationspring.service;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Service
public class ConnectionServiceImpl implements ConnectionService {

	static final String URL_LOCAL = "jdbc:postgresql://localhost:5432/soopdb";
	static final String USER_LOCAL = "";
	static final String PASSWORD_LOCAL = "";

	/**
	 * Connect to the database
	 *
	 * @return a Connection object
	 */
	@Override
	public Connection connect(String url, String login, String password) throws SQLException {
		Connection connection = DriverManager.getConnection(url, login, password);
		System.out.println("Connected to the PostgreSQL server successfully.\n");

		return connection;
	}
}
