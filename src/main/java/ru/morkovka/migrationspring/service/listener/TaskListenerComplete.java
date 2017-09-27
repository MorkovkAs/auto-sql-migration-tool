package ru.morkovka.migrationspring.service.listener;

public interface TaskListenerComplete extends TaskListener {

	void taskComplete();
}