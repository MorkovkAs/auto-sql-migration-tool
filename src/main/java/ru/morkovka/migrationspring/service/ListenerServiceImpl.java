package ru.morkovka.migrationspring.service;

import com.samczsun.skype4j.exceptions.ConnectionException;
import org.springframework.stereotype.Service;
import ru.morkovka.migrationspring.service.listener.TaskListener;
import ru.morkovka.migrationspring.service.listener.TaskListenerComplete;
import ru.morkovka.migrationspring.service.listener.TaskListenerError;
import ru.morkovka.migrationspring.service.listener.TaskListenerPartComplete;
import ru.morkovka.migrationspring.utils.SkypeUtils;

import java.util.ArrayList;
import java.util.Collections;

@Service
public class ListenerServiceImpl implements ListenerService {

	private java.util.List<TaskListenerComplete> listenersComplete = Collections.synchronizedList(new ArrayList<TaskListenerComplete>());

	private java.util.List<TaskListenerError> listenersError = Collections.synchronizedList(new ArrayList<TaskListenerError>());

	private java.util.List<TaskListenerPartComplete> listenersQueryUpdateComplete = Collections.synchronizedList(new ArrayList<TaskListenerPartComplete>());

	public void notifyListenersComplete() {
		synchronized (listenersComplete) {
			for (TaskListenerComplete listener : listenersComplete) {
				listener.taskComplete();
			}
		}
		try {
			SkypeUtils.logoutFromSkype();
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}

	public final void notifyListenersQueryUpdateComplete(String message) {
		synchronized (listenersQueryUpdateComplete) {
			for (TaskListenerPartComplete listener : listenersQueryUpdateComplete) {
				listener.taskPartComplete(message);
			}
		}
	}

	public final void notifyListenersError() {
		synchronized (listenersError) {
			for (TaskListenerError listener : listenersError) {
				listener.taskError();
			}
		}
	}

	public void addListener(TaskListener listener) {
		if (listener instanceof TaskListenerComplete) {
			listenersComplete.add((TaskListenerComplete) listener);
		} else if (listener instanceof TaskListenerPartComplete) {
			listenersQueryUpdateComplete.add((TaskListenerPartComplete) listener);
		} else if (listener instanceof TaskListenerError) {
			listenersError.add((TaskListenerError) listener);
		}
	}

	public void deleteListener(TaskListener listener) {
		if (listener instanceof TaskListenerComplete) {
			listenersComplete.remove(listener);
		} else if (listener instanceof TaskListenerPartComplete) {
			listenersQueryUpdateComplete.remove(listener);
		} else if (listener instanceof TaskListenerError) {
			listenersError.remove(listener);
		}
	}
}