package ru.morkovka.migrationspring.service;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionService {

	Connection connect(String url, String login, String password) throws SQLException;
}
