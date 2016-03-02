package org.t2k.interactions.controllers;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import org.t2k.interactions.models.FileData;
import org.t2k.interactions.services.files.FileService;
import org.t2k.interactions.utils.GeneralUtils;
import org.t2k.interactions.utils.InteractionsConfig;
import org.t2k.interactions.utils.jsonUtils.JsonWrapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 28/07/2015
 * Time: 15:31
 */
@WebAppConfiguration
@ContextConfiguration("classpath:/springContext/applicationContext-rest.xml")
public class FileControllerTest extends AbstractTestNGSpringContextTests {

    private final String TIME_TO_KNOW_IMAGE_FILE = "timeToKnow.png";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FileService fileService;

    @Autowired
    private InteractionsConfig interactionsConfig;

    @Autowired
    private JsonWrapper jsonWrapper;

    private List<String> sha1Files;

//    @BeforeClass
//    public void setup() {
//        // Setup web application mock
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
//
//        // Initialize sha1Files list
//        sha1Files = new ArrayList<>();
//    }

//    @AfterMethod
//    public void cleanup() {
//        sha1Files.forEach(sha1 -> {
//            try {
//                FileData fileData = fileService.getFileDataListBySha1(sha1).get(0);
//                fileService.deleteFileFromDiskAndItsRelevantFileDataFromDb(fileData);
//            } catch (DbException e) {
//                logger.error(String.format("Failed to delete the test file data: %s from DB after %s had finished.", sha1, this.getClass().getSimpleName()));
//            } catch (IOException e) {
//                logger.error(String.format("Failed to delete the test file: %s from Disk after %s had finished.", sha1, this.getClass().getSimpleName()));
//            }
//        });
//    }

    @Test(enabled = false)
    public void uploadFileTest() throws Exception {
        FileInputStream fis = new FileInputStream(String.format("%s/%s", GeneralUtils.getResource(this.getClass(), "files").getPath(), TIME_TO_KNOW_IMAGE_FILE));
        MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
        String sha1String = DigestUtils.sha1Hex(multipartFile.getBytes());
        sha1Files.add(sha1String);
        String fileUrl = String.format("%s%s", interactionsConfig.getProperty("files.url"), sha1String);

        MvcResult mvcResult = mockMvc.perform(fileUpload("/files")
                .file(multipartFile)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(String.format("\"%s\"", fileUrl)))
                .andReturn();



        // Validate the file's data was saved in DB
        List<FileData> fileDataListBySha1 = fileService.getFileDataListBySha1(sha1String);
        Assert.assertEquals(fileDataListBySha1.size(), 1, String.format("There should be only 1 file data for sha1: %s in DB but found %d.", sha1String, fileDataListBySha1.size()));
        FileData fileDataFromDb = fileDataListBySha1.get(0);
        Assert.assertEquals(sha1String, fileDataFromDb.getSha1());
        Assert.assertEquals(fileUrl, fileDataFromDb.getUrl());
        Assert.assertEquals("dummy-user", fileDataFromDb.getUsername());

        // Validate the file was saved to disk
        File file = new File(fileDataFromDb.getPath());
        Assert.assertTrue(file.exists());
    }
}