package ru.morkovka.migrationspring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.morkovka.migrationspring.service.AuthenticationService;

import java.util.List;

@RestController
@RequestMapping("/key")
public class AuthenticationController {

	@Autowired
	private AuthenticationService authenticationService;

	@RequestMapping(value = "check/{key}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	boolean checkKey(@PathVariable("key") String key) {
		return authenticationService.checkKey(key);
	}

	@RequestMapping(value = "delete/{key}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	boolean deleteKey(@PathVariable("key") String key) {
		return authenticationService.deleteKey(key);
	}

	@RequestMapping(value = "new/{n}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<String> generateNewKeys(@PathVariable("n") int n) {
		return authenticationService.generateNewKeys(n);
	}
}
