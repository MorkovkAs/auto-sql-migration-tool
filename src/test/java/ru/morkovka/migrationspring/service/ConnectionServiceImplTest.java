package ru.morkovka.migrationspring.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;

import static ru.morkovka.migrationspring.service.ConnectionServiceImpl.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConnectionServiceImplTest {

	@Autowired
	private ConnectionService connectionService;

	@Test
	public void connectLocal() throws SQLException {
		connectionService.connect(URL_LOCAL, USER_LOCAL, PASSWORD_LOCAL);
	}
}