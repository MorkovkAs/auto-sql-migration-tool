package ru.morkovka.migrationspring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.morkovka.migrationspring.service.MigrationService;

@RestController
@RequestMapping("/migration")
public class MigrationController {

	@Autowired
	private MigrationService migrationService;

	@RequestMapping(value = "/{key}/start", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	boolean startMigration(@PathVariable("key") String key) {
		return migrationService.validatePermissionAndStartMigration(key);
	}
}
