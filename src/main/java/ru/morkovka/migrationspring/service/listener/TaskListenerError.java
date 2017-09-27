package ru.morkovka.migrationspring.service.listener;

public interface TaskListenerError extends TaskListener {

	void taskError();
}