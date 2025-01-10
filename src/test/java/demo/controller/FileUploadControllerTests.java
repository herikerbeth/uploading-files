package demo.controller;

import demo.TestData;
import demo.exception.StorageFileNotFoundException;
import demo.service.StorageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileUploadController.class)
public class FileUploadControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StorageService service;

    @Test
    void testListUploadedFilesSuccessfully() throws Exception {
        when(service.loadAll()).thenReturn(Stream.of(
                Path.of("file.txt"),
                Path.of("file1.txt")
        ));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("files"))
                .andExpect(content().string(containsString("file.txt")))
                .andExpect(content().string(containsString("file1.txt")));
    }

    @Test
    void testServeFileSuccessfully() throws Exception {
        String filename = "test.txt";
        Resource resource = Mockito.mock(Resource.class);

        when(service.loadAsResource(filename)).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.isReadable()).thenReturn(true);
        when(resource.getFilename()).thenReturn(filename);

        mockMvc.perform(get("/files/{filename}", filename).accept(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.txt\""));
    }

    @Test
    void testServeFileNotFound() throws Exception {
        String filename = null;

        when(service.loadAsResource(null))
                .thenThrow(new StorageFileNotFoundException("Could not read file: " + filename));

        mockMvc.perform(get("/files/{filename}", filename).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testHandleFileUpload() throws Exception {
        MockMultipartFile file = TestData.createTestFile();

        mockMvc.perform(multipart("/").file(file))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(header().string("Location", "/"))
                        .andExpect(flash().attribute("message", "You successfully upload test.txt!"));

        verify(service).store(Mockito.any());
    }
}
