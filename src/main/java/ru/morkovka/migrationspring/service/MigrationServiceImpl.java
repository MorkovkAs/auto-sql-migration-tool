package ru.morkovka.migrationspring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.morkovka.migrationspring.utils.ReadingUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.exit;
import static ru.morkovka.migrationspring.service.ConnectionServiceImpl.*;
import static ru.morkovka.migrationspring.utils.ListenerUtils.*;
import static ru.morkovka.migrationspring.utils.LoggingUtils.*;
import static ru.morkovka.migrationspring.utils.SkypeUtils.CHAT_ME;

@Service
public class MigrationServiceImpl implements MigrationService {
	private static Logger logger;

	public final static long DELTA = 50000;
	private final static int MAX_NUMBER_OF_FAILS = 5;
	private final static int SPRINT = 67;

	@Autowired
	private ConnectionService connectionService;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private ListenerService listenerService;

	@Override
	public boolean validatePermissionAndStartMigration(String key) {
		if (authenticationService.checkAndDeleteKeyIfExist(key)) {
			listenerService.addListener(createListenerSkypeQueryUpdateComplete(CHAT_ME));
			listenerService.addListener(createListenerSkypeComplete(CHAT_ME));
			listenerService.addListener(createListenerSkypeError(CHAT_ME));

			startMigration();
			return true;
		}
		return false;
	}

	private void startMigration() {
		LocalDateTime startMigrationTime = LocalDateTime.now();
		if (logger == null) {
			logger = createLogger();
		}
		if (logger == null) {
			return;
		}

		//todo check connection LOCAL or PROD
		try (Connection connection = connectionService.connect(URL_LOCAL, USER_LOCAL, PASSWORD_LOCAL)) {
			connection.setAutoCommit(false);

			ReadingUtils.getQueriesInfoBySprint(SPRINT)
					.forEach(queryInfo -> queryPartialUpdate(queryInfo.get(0), queryInfo.get(1), queryInfo.get(2), connection));
			//or
			//queryPartialUpdate(Queries.q1, "isod.tbcase_protocol", "protocol_id", connection);

			listenerService.notifyListenersComplete();
			System.out.println("Migration complete.");

		} catch (Exception e) {
			System.out.println("Exception in program. Exit.");
			logger.log(Level.SEVERE, "Exception in program", e);
			logger.log(Level.INFO, getBeforeMigrationExitLog(startMigrationTime));
			listenerService.notifyListenersError();
		}
		listenerService.notifyListenersComplete();
	}

	private void wait(int k) {
		long time0, time1;
		time0 = System.currentTimeMillis();
		do {
			time1 = System.currentTimeMillis();
		}
		while ((time1 - time0) < k * 1000);
	}

	private void queryInsertOrDelete(String query, String table, String idName, Connection connection) {
		LocalDateTime startTime = LocalDateTime.now();
		boolean success = false;

		logger.log(Level.INFO, "Start processing");

		if (Thread.currentThread().isInterrupted()) {
			return;
		}
		checkTheEndOfTimeNight(query, table, idName);

		try {
			success = executeStatement(connection, query);
			connection.commit();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Exception in delta", e);
			try {
				connection.rollback();
				logger.log(Level.WARNING, getFullErrorLog());
			} catch (SQLException e1) {
				logger.log(Level.SEVERE, null, e);
			}
		}

		System.out.println(query);
		String beforeExitLog = getBeforeExitLog(startTime, success);
		System.out.println(beforeExitLog + "\n\n");
		logger.log(Level.INFO, beforeExitLog);
		listenerService.notifyListenersQueryUpdateComplete(query.substring(0, query.indexOf("\n")) + " ...\n" + beforeExitLog);
	}

	private void queryUpdate(String query, String table, String idName, Connection connection) {
		LocalDateTime startTime = LocalDateTime.now();
		long totalUpdated = 0;
		int effectedRows;
		//wait(5);

		logger.log(Level.INFO, "Start processing");

		if (Thread.currentThread().isInterrupted()) {
			return;
		}
		checkTheEndOfTimeNight(query, table, idName);

		try {
			effectedRows = executeUpdate(connection, query);
			connection.commit();
			totalUpdated = effectedRows;
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Exception in delta", e);
			try {
				connection.rollback();
				logger.log(Level.WARNING, getFullErrorLog());
			} catch (SQLException e1) {
				logger.log(Level.SEVERE, null, e);
			}
		}

		System.out.println(query);
		String beforeExitLog = getBeforeExitLog(startTime, totalUpdated, 0);
		System.out.println(beforeExitLog + "\n\n");
		logger.log(Level.INFO, beforeExitLog);
		listenerService.notifyListenersQueryUpdateComplete(query.substring(0, query.indexOf("\n")) + " ...\n" + beforeExitLog);
	}

	private void queryPartialUpdate(String query, String table, String idName, Connection connection) {
		LocalDateTime startTime = LocalDateTime.now();
		long leftBorder;
		long rightBorder;
		try {
			leftBorder = this.findMinMax(connection, Param.MIN, idName, table);
			rightBorder = this.findMinMax(connection, Param.MAX, idName, table);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		long totalRows = rightBorder - leftBorder + 1;
		long currentRightBorder;
		long totalUpdated = 0;
		long totalFailed = 0;
		int effectedRows;

		logger.log(Level.INFO, String.format("Delta =      %10s", DELTA));
		logger.log(Level.INFO, String.format("Min value =  %10s", leftBorder));
		logger.log(Level.INFO, String.format("Max value =  %10s", rightBorder));
		logger.log(Level.INFO, String.format("Total rows = %10s", totalRows));
		logger.log(Level.INFO, "Start processing");

		for (long currentLeftBorder = leftBorder; currentLeftBorder < rightBorder + DELTA; currentLeftBorder += DELTA) {
			if (Thread.currentThread().isInterrupted()) {
				return;
			}
			//checkTheEndOfTimeNight(query, table, idName, currentLeftBorder);
			currentRightBorder = currentLeftBorder + DELTA - 1;

			try {
				effectedRows = executePartialUpdate(connection, query, currentLeftBorder, currentRightBorder);
				connection.commit();
				totalUpdated += effectedRows;
				logger.log(Level.INFO, getDeltaLog(leftBorder, rightBorder, totalRows, currentRightBorder, effectedRows));
			} catch (SQLException e) {
				totalFailed++;

				logger.log(Level.SEVERE, "Exception in delta", e);
				try {
					connection.rollback();
					logger.log(Level.WARNING, getDeltaErrorLog(currentLeftBorder, currentRightBorder));
				} catch (SQLException e1) {
					logger.log(Level.SEVERE, null, e);
				}
				if (totalFailed > MAX_NUMBER_OF_FAILS) {
					return;
				}
			}
		}
		System.out.println(query);
		String beforeExitLog = getBeforeExitLog(startTime, totalUpdated, totalFailed);
		System.out.println(beforeExitLog + "\n\n");
		logger.log(Level.INFO, beforeExitLog);
		listenerService.notifyListenersQueryUpdateComplete(query.substring(query.indexOf("UPDATE "), query.indexOf("\n", query.indexOf("UPDATE "))) + " ...\n" + beforeExitLog);
	}

	private void checkTheEndOfTimeNight(String query, String table, String idName) {
		if (LocalDateTime.now().getHour() == 2 && LocalDateTime.now().getMinute() == 50) {
			System.out.println("Abort because of time!");
			System.out.println("Query: " + query);
			System.out.println("Table: " + table);
			System.out.println("IdName: " + idName);
			exit(666);
		}
	}

	private void checkTheEndOfTimeNight(String query, String table, String idName, long currentLeftBorder) {
		if (LocalDateTime.now().getHour() == 2 && LocalDateTime.now().getMinute() == 50) {
			System.out.println("Abort because of time!");
			System.out.println("Query: " + query);
			System.out.println("Table: " + table);
			System.out.println("IdName: " + idName);
			System.out.println("currentLeftBorder: " + currentLeftBorder);
			exit(666);
		}
	}

	/**
	 * Find minimum or maximum of a column <code>column_name</code> of a <code>table</code>
	 *
	 * @param param       {@link Param#MIN} or {@link Param#MAX}
	 * @param column_name to find min or max
	 * @param table       to select from
	 * @return minimum or maximum value of selected column
	 */
	private long findMinMax(Connection connection, Param param, String column_name, String table) throws SQLException {
		Statement statement = connection.createStatement();
		String query = String.format("SELECT %s(%s) FROM %s", param, column_name, table);
		ResultSet result = statement.executeQuery(query);

		return result.next() ? result.getLong(param.toString()) : 0;
	}

	/**
	 * Count all rows in <code>table</code>
	 *
	 * @param table to select from
	 * @return count of all rows in <code>table</code>
	 */
	private long selectCount(Connection connection, String table) throws SQLException {
		Statement statement = connection.createStatement();
		String query = String.format("SELECT COUNT(*) FROM isod.%s", table);
		ResultSet result = statement.executeQuery(query);

		return result.next() ? result.getLong("COUNT") : 0;
	}

	private ResultSet executePartialSelect(Connection connection, String query, long currentLeftBorder, long currentRightBorder) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		preparedStatement.setLong(1, currentLeftBorder);
		preparedStatement.setLong(2, currentRightBorder);

		return preparedStatement.executeQuery();
	}

	private boolean executeStatement(Connection connection, String query) throws SQLException {
		Statement statement = connection.createStatement();
		return statement.execute(query);
	}

	private int executeUpdate(Connection connection, String query) throws SQLException {
		Statement statement = connection.createStatement();
		return statement.executeUpdate(query);
	}

	private int executePartialUpdate(Connection connection, String query, long currentLeftBorder, long currentRightBorder) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		preparedStatement.setLong(1, currentLeftBorder);
		preparedStatement.setLong(2, currentRightBorder);

		return preparedStatement.executeUpdate();
	}

	private String getAllIdFromResultSet(ResultSet resultSet) throws SQLException {
		StringBuilder allIds = new StringBuilder();
		while (resultSet.next()) {
			allIds.append(",").append(resultSet.getLong(1));
		}

		return (allIds.length() == 0) ? "" : allIds.substring(1);
	}

	enum Param {
		MIN, MAX
	}
}
