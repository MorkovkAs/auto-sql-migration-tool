package ru.morkovka.migrationspring.utils;

import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.SkypeBuilder;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.InvalidCredentialsException;
import com.samczsun.skype4j.exceptions.NotParticipatingException;

public class SkypeUtils {
	public static final String CHAT_ME = "8:live:anton.android.apps";

	private static Skype skype;
	private static final String USER_NAME = "";
	private static final String PASSWORD = "";

	static Skype loginToSkype() throws ConnectionException, InvalidCredentialsException, NotParticipatingException {
		if (skype == null) {
			skype = new SkypeBuilder(USER_NAME, PASSWORD).withAllResources().withExceptionHandler((errorSource, throwable, willShutdown) -> {
				System.out.println("Error: " + errorSource + " " + throwable + " " + willShutdown);
			}).build();

			skype.login();
			skype.subscribe();
			System.out.println("Connected");
		}

		return skype;
	}

	public static void logoutFromSkype() throws ConnectionException {
		skype.logout();
	}
}
