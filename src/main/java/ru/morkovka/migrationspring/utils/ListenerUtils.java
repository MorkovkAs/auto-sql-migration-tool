package ru.morkovka.migrationspring.utils;

import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.InvalidCredentialsException;
import ru.morkovka.migrationspring.service.listener.TaskListenerComplete;
import ru.morkovka.migrationspring.service.listener.TaskListenerError;
import ru.morkovka.migrationspring.service.listener.TaskListenerPartComplete;

import static ru.morkovka.migrationspring.utils.SkypeUtils.loginToSkype;

public class ListenerUtils {

	public static TaskListenerComplete createListenerSkypeComplete(String chatName) {
		return () -> {
			try {
				Skype skype = loginToSkype();
				Chat chat = skype.getOrLoadChat(chatName);
				chat.sendMessage("Migration complete.");
			} catch (ConnectionException | InvalidCredentialsException | ChatNotFoundException e) {
				e.printStackTrace();
			}
		};
	}

	public static TaskListenerPartComplete createListenerSkypeQueryUpdateComplete(String chatName) {
		return (message) -> {
			try {
				Skype skype = loginToSkype();
				Chat chat = skype.getOrLoadChat(chatName);
				chat.sendMessage("Task complete:\n" + message);
			} catch (ConnectionException | InvalidCredentialsException | ChatNotFoundException e) {
				e.printStackTrace();
			}
		};
	}

	public static TaskListenerError createListenerSkypeError(String chatName) {
		return () -> {
			try {
				Skype skype = loginToSkype();
				Chat chat = skype.getOrLoadChat(chatName);
				chat.sendMessage("Task failed.");
			} catch (ConnectionException | InvalidCredentialsException | ChatNotFoundException e) {
				e.printStackTrace();
			}
		};
	}
}
