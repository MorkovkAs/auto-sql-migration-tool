package ru.morkovka.migrationspring.service.listener;

public interface TaskListenerPartComplete extends TaskListener {

	void taskPartComplete(String message);
}