package demo;

import demo.config.StorageProperties;
import demo.controller.FileUploadController;
import demo.exception.StorageException;
import demo.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class ApplicationTests {

	@Autowired
	private FileUploadController fileUploadController;

	@Autowired
	private StorageService storageService;

	@Autowired
	private StorageProperties storageProperties;

	@Test
	void contextLoads() {
		assertThat(fileUploadController).isNotNull();

		assertThat(storageService).isNotNull();

		assertThat(storageProperties).isNotNull();
		assertThat(storageProperties.getLocation()).isEqualTo("upload-dir");

		try {
			storageService.init();
		} catch (StorageException e) {
			assertThat(e).isNull();
		}
	}
}
