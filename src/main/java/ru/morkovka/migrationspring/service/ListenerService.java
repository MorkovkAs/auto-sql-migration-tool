package ru.morkovka.migrationspring.service;

import ru.morkovka.migrationspring.service.listener.TaskListener;

public interface ListenerService {

	void notifyListenersComplete();

	void notifyListenersQueryUpdateComplete(String message);

	void notifyListenersError();

	void addListener(TaskListener listener);

	void deleteListener(TaskListener listener);
}