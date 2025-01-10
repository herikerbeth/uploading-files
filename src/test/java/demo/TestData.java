package demo;

import org.springframework.mock.web.MockMultipartFile;

public class TestData {
    public TestData() {
    }

    public static MockMultipartFile createTestFile() {
        return new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Test content".getBytes()
        );
    }

    public static MockMultipartFile createEmptyFile() {
        return new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                new byte[0]
        );
    }

    public static MockMultipartFile createFileOutsideCurrentDirectory() {
        return new MockMultipartFile(
                "file",
                "../outside.txt",
                "text/plain",
                "content".getBytes()
        );
    }
}
