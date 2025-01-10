package demo.service;

import demo.TestData;
import demo.config.StorageProperties;
import demo.exception.StorageException;
import demo.exception.StorageFileNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileSystemStorageServiceTests {

    private FileSystemStorageService storageService;

    @Mock
    private StorageProperties properties;

    private final Path rootLocation = Paths.get("test-upload-dir");

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        when(properties.getLocation()).thenReturn("test-upload-dir");

        storageService = new FileSystemStorageService(properties);

        // Ensure the directory exists
        Files.createDirectories(rootLocation);
    }

    // Cleanup after each test
    @AfterEach
    void tearDown() throws IOException {
        FileSystemUtils.deleteRecursively(rootLocation);
    }

    @Test
    void testInitializationFailsWhenLocationIsEmpty() {
        // Configure Storage Properties to return an empty path
        when(properties.getLocation()).thenReturn("");

        // Checks if the expected exception is thrown
        StorageException exception = assertThrows(StorageException.class, () -> new FileSystemStorageService(properties));
        assertEquals("File updload location can not be Empty.", exception.getMessage());
    }

    @Test
    void testStoreFileSuccessfully() throws IOException {
        MockMultipartFile file = TestData.createTestFile();

        storageService.store(file);

        // Check if the file was saved correctly
        Path storedFile = rootLocation.resolve("test.txt");
        assertTrue(Files.exists(storedFile));
        assertEquals("Test content", Files.readString(storedFile));
    }

    @Test
    void testStoreEmptyFileThrowsException() {
        MockMultipartFile file = TestData.createEmptyFile();

        StorageException exception = assertThrows(
                StorageException.class,
                () -> storageService.store(file)
        );

        assertEquals("Failed to store empty file.", exception.getMessage());
    }

    @Test
    void testStoreFileOutsideCurrentDirectoryThrowsException() {
        MockMultipartFile file = TestData.createFileOutsideCurrentDirectory();

        StorageException exception = assertThrows(
                StorageException.class,
                () -> storageService.store(file)
        );

        assertEquals("Cannot store file outside current directory.", exception.getMessage());
    }

    @Test
    void testLoadAllSuccessfully() throws IOException {
        // Prepare mock files in the test directory
        Files.createFile(rootLocation.resolve("file.txt"));
        Files.createFile(rootLocation.resolve("file1.txt"));

        // Execute the method to be tested
        Stream<Path> result = storageService.loadAll();

        // Verify that files are being uploaded correctly
        assertNotNull(result);
        assertEquals(2, result.count());

        // Cleaning
        Files.deleteIfExists(rootLocation.resolve("file.txt"));
        Files.deleteIfExists(rootLocation.resolve("file1.txt"));
    }

    @Test
    void testLoadAllThrowsExceptionOnFailure() {
        // Simulates an exception in the Files.walk method
        try {
            FileSystemUtils.deleteRecursively(rootLocation); // Remove directory to cause error
        }
        catch (IOException e) {
            fail("Failed to delete test directory during setup for the test");
        }

        //  Checks if the method throws the expected exception
        StorageException exception = assertThrows(StorageException.class, () -> storageService.loadAll());
        assertTrue(exception.getMessage().contains("Failed to read stored files"));
    }

    @Test
    void testLoadSuccessfully() {
        String filename = "test.txt";
        Path expectedPath = rootLocation.resolve(filename);

        Path result = storageService.load(filename);

        assertEquals(expectedPath, result);
    }

    @Test
    void testLoadWithInvalidPath() {
        String invalidFilename = "../invalid.txt";

        // Should still resolve but remain within the root directory
        Path result = storageService.load(invalidFilename);
        Path expectedPath = rootLocation.resolve(invalidFilename);

        assertEquals(expectedPath, result);
    }

    @Test
    void testLoadAsResourceSuccessfully() throws IOException {
        String filename = "test.txt";
        Path filePath = rootLocation.resolve(filename);
        Files.write(filePath, "Hello, World!".getBytes());

        Resource resource = storageService.loadAsResource(filename);

        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
        assertEquals(filePath.toUri(), resource.getURI());
    }

    @Test
    void testLoadAsResourceThrowExceptionWhenFileDoesNotExist() {
        String filename = "nonexistent-file.txt";

        StorageFileNotFoundException exception = assertThrows(
                StorageFileNotFoundException.class,
                () -> storageService.loadAsResource(filename)
        );

        assertTrue(exception.getMessage().contains("Could not read file: " + filename));
    }

    @Test
    void testDeleteAllSuccessfully() throws IOException {
        Path testFile = rootLocation.resolve("test.txt");
        Files.createFile(testFile);

        assertTrue(Files.exists(testFile), "File must exist before calling deleteAll()");

        storageService.deleteAll();

        assertFalse(Files.exists(testFile), "Directory must be deleted after deleteAll()");
    }

    @Test
    void testInitSuccessfully() {
        storageService.init();

        assertTrue(Files.exists(rootLocation));
    }

    @Test
    void testInitFailsWhenDirectoryCannotBeCreated() throws IOException {
        FileSystemUtils.deleteRecursively(rootLocation);
        Files.createFile(rootLocation);

        StorageException exception = assertThrows(StorageException.class,
                storageService::init);

        assertTrue(exception.getMessage().contains("Could not initialize storage"));
        Files.deleteIfExists(rootLocation);
    }
}
