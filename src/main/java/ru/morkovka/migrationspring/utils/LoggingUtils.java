package ru.morkovka.migrationspring.utils;

import ru.morkovka.migrationspring.service.MigrationServiceImpl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggingUtils {

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

	/**
	 * Create logger
	 */
	public static Logger createLogger() {
		try {
			LogManager.getLogManager().readConfiguration(MigrationServiceImpl.class.getResourceAsStream("/logging.properties"));
			return Logger.getLogger(MigrationServiceImpl.class.getName());
		} catch (IOException e) {
			System.err.println("Could not setup logger configuration: " + e.toString());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Format log after every {@value MigrationServiceImpl#DELTA} iteration
	 *
	 * @param leftBorder         minimum Id of SELECT
	 * @param rightBorder        maximum Id of SELECT
	 * @param totalRows          number of all rows in the table
	 * @param currentRightBorder last value of {@value MigrationServiceImpl#DELTA} block
	 * @param effectedRows       number of updated rows
	 * @return String for log with general processing info
	 */
	public static String getDeltaLog(long leftBorder, long rightBorder, long totalRows, long currentRightBorder, long effectedRows) {
		return String.format("Updated in delta %7.3f%%\t\tRows processed %5.1f%%\t\tId processed %10s",
				100.0 * effectedRows / MigrationServiceImpl.DELTA,
				currentRightBorder < rightBorder ? 100.0 * (currentRightBorder - leftBorder) / totalRows : 100.0,
				currentRightBorder);
	}

	/**
	 * Format error log.
	 * For example if exception occurred
	 *
	 * @return String for error log with general processing info
	 */
	public static String getFullErrorLog() {
		return String.format("%-12s\tTransaction is being rolled back.", TIME_FORMATTER.format(LocalTime.now()));
	}

	/**
	 * Format error log during {@value MigrationServiceImpl#DELTA} iteration.
	 * For example if exception occurred
	 *
	 * @param currentLeftBorder  first value of {@value MigrationServiceImpl#DELTA} block
	 * @param currentRightBorder last value of {@value MigrationServiceImpl#DELTA} block
	 * @return String for error log with general processing info
	 */
	public static String getDeltaErrorLog(long currentLeftBorder, long currentRightBorder) {
		return String.format("%-12s\tTransaction is being rolled back.\tLeft id = %d\tRight id = %d",
				TIME_FORMATTER.format(LocalTime.now()), currentLeftBorder, currentRightBorder);
	}

	/**
	 * Format error log during {@value MigrationServiceImpl#DELTA} iteration.
	 * For example if exception occurred
	 *
	 * @param startTime    start time
	 * @param totalUpdated number of updated rows during execution
	 * @param totalFailed  number of failed {@value MigrationServiceImpl#DELTA} blocks during execution
	 * @return String for before exit log with general after execution info
	 */
	public static String getBeforeExitLog(LocalDateTime startTime, long totalUpdated, long totalFailed) {
		LocalDateTime endTime = LocalDateTime.now();

		return String.format("%-12s\tTotal updated\t%d rows in %s\n\t\t\t\tTotal failed\t%d", TIME_FORMATTER.format(endTime), totalUpdated, getExecutedTime(startTime, endTime), totalFailed);
	}

	/**
	 * Format error log during {@value MigrationServiceImpl#DELTA} iteration.
	 * For example if exception occurred
	 *
	 * @param startTime start time
	 * @param success   status of query execution
	 * @return String for before exit log with general after execution info
	 */
	public static String getBeforeExitLog(LocalDateTime startTime, boolean success) {
		LocalDateTime endTime = LocalDateTime.now();

		return String.format("%-12s\tSuccess\t%s, in %s", TIME_FORMATTER.format(endTime), success, getExecutedTime(startTime, endTime));
	}

	/**
	 * Format error log during {@value MigrationServiceImpl#DELTA} iteration.
	 * For example if exception occurred
	 *
	 * @param startTime start time
	 * @return String for before exit log with general after execution info
	 */
	public static String getBeforeMigrationExitLog(LocalDateTime startTime) {
		LocalDateTime endTime = LocalDateTime.now();

		return String.format("%-12s\t all time %s", TIME_FORMATTER.format(endTime), getExecutedTime(startTime, endTime));
	}

	/**
	 * Calculates full executed time
	 *
	 * @param from start time
	 * @param to   end time
	 * @return String with number of hours, minutes, seconds
	 */
	private static String getExecutedTime(LocalDateTime from, LocalDateTime to) {
		long hours = from.until(to, ChronoUnit.HOURS);
		from = from.plusHours(hours);
		long minutes = from.until(to, ChronoUnit.MINUTES);
		from = from.plusMinutes(minutes);
		long seconds = from.until(to, ChronoUnit.SECONDS);

		return "" + (hours != 0 ? hours + " hours " : "") + (minutes != 0 ? minutes + " minutes " : "") + (seconds != 0 ? seconds + " seconds " : "");
	}
}
