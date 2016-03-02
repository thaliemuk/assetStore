package org.t2k.interactions.services;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.t2k.interactions.models.FileData;
import org.t2k.interactions.models.exceptions.DbException;
import org.t2k.interactions.models.exceptions.EmptyFileException;
import org.t2k.interactions.services.files.FileService;
import org.t2k.interactions.utils.GeneralUtils;
import org.t2k.interactions.utils.InteractionsConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 02/08/2015
 * Time: 15:19
 */
@ContextConfiguration("classpath:/springContext/applicationContext-service.xml")
public class FileServiceTest  extends AbstractTestNGSpringContextTests {

    @Autowired
    private FileService fileService;

    @Autowired
    private InteractionsConfig interactionsConfig;

    private final String DID_YOU_KNOW_IMAGE_FILE = "didYouKnow.png";
    private final String TIME_ICON_IMAGE_FILE = "timeIcon.png";

    private Set<String> sha1Files;

//    @BeforeClass
//    public void setup() {
//        // Initialize sha1Files list
//        sha1Files = new HashSet<>();
//    }
//
//    @AfterTest
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

    @Test(priority = 0, enabled = false)
    public void uploadFileOnce() throws IOException, EmptyFileException, DbException {
        String username = "user-test1";
        String fileUrl = uploadFile(DID_YOU_KNOW_IMAGE_FILE, username);
        String sha1 = getSha1FromUrl(fileUrl);

        // validate file data was saved to DB
        List<FileData> fileDataListBySha1 = fileService.getFileDataListBySha1(sha1);
        Assert.assertEquals(fileDataListBySha1.size(), 1, String.format("There should be only 1 file data for sha1: %s in DB but found %d.", sha1, fileDataListBySha1.size()));
        FileData fileDataFromDb = fileDataListBySha1.get(0);
        validateFileOnDisk(fileDataFromDb);
        Assert.assertEquals(fileUrl, fileDataFromDb.getUrl());
        Assert.assertEquals(username, fileDataFromDb.getUsername());
    }

    @Test(dependsOnMethods = "uploadFileOnce", priority = 1, enabled = false)
    public void uploadSameFileTwiceBySameUser() throws IOException, EmptyFileException, DbException {
        String username = "user-test1";
        String fileUrl = uploadFile(DID_YOU_KNOW_IMAGE_FILE, username);
        String sha1 = getSha1FromUrl(fileUrl);

        // validate file data was saved to DB
        List<FileData> fileDataListBySha1 = fileService.getFileDataListByUsername(username);
        Assert.assertEquals(fileDataListBySha1.size(), 1, String.format("There should be only 1 file data for sha1: %s in DB but found %d.", sha1, fileDataListBySha1.size()));
        FileData fileDataFromDb = fileDataListBySha1.get(0);
        validateFileOnDisk(fileDataFromDb);
        Assert.assertEquals(sha1, fileDataFromDb.getSha1());
        Assert.assertEquals(fileUrl, fileDataFromDb.getUrl());
        Assert.assertEquals(username, fileDataFromDb.getUsername());
    }

    @Test(dependsOnMethods = "uploadFileOnce", priority = 2, enabled = false)
    public void uploadSameFileTwiceByDifferentUsers() throws EmptyFileException, IOException, DbException {
        String username = "user-test2";
        String fileUrl = uploadFile(DID_YOU_KNOW_IMAGE_FILE, username);
        String sha1 = getSha1FromUrl(fileUrl);

        // validate file data was saved to DB for each user
        List<FileData> fileDataListBySha1 = fileService.getFileDataListBySha1(sha1);
        Assert.assertEquals(fileDataListBySha1.size(), 2, String.format("There should be 2 file data for sha1: %s in DB but found %d.", sha1, fileDataListBySha1.size()));

        FileData firstFileDataFromDb;
        FileData secondFileDataFromDb;
        // decide which file was the first by checking which one's username equals to this fileData's username which occurred second
        if (fileDataListBySha1.get(0).getUsername().equals(username)) {
            firstFileDataFromDb = fileDataListBySha1.get(1);
            secondFileDataFromDb = fileDataListBySha1.get(0);
        } else {
            firstFileDataFromDb = fileDataListBySha1.get(0);
            secondFileDataFromDb = fileDataListBySha1.get(1);
        }

        // validate first user
        validateFileOnDisk(firstFileDataFromDb);
        Assert.assertEquals(sha1, firstFileDataFromDb.getSha1());
        Assert.assertEquals(fileUrl, firstFileDataFromDb.getUrl());
        Assert.assertNotEquals(username, firstFileDataFromDb.getUsername());
        // validate second user
        validateFileOnDisk(secondFileDataFromDb);
        Assert.assertEquals(sha1, secondFileDataFromDb.getSha1());
        Assert.assertEquals(fileUrl, secondFileDataFromDb.getUrl());
        Assert.assertEquals(username, secondFileDataFromDb.getUsername());
    }

    @Test(dependsOnMethods = "uploadFileOnce", priority = 3, enabled = false)
    public void uploadDifferentFilesBySameUser() throws EmptyFileException, IOException, DbException {
        String username = "user-test1";
        String fileUrl = uploadFile(TIME_ICON_IMAGE_FILE, username);
        String sha1 = getSha1FromUrl(fileUrl);

        // validate file data was saved to DB for each file with the same username
        List<FileData> fileDataListByUsername = fileService.getFileDataListByUsername(username);
        Assert.assertEquals(fileDataListByUsername.size(), 2, String.format("There should be 2 file data for username: %s in DB but found %d.", username, fileDataListByUsername.size()));

        FileData firstFileDataFromDb;
        FileData secondFileDataFromDb;
        // decide which file was the first by checking which one's sha1 equals to this fileUrl's sha1 which occurred second
        if (fileDataListByUsername.get(0).getUrl().equals(fileUrl)) {
            firstFileDataFromDb = fileDataListByUsername.get(1);
            secondFileDataFromDb = fileDataListByUsername.get(0);
        } else {
            firstFileDataFromDb = fileDataListByUsername.get(0);
            secondFileDataFromDb = fileDataListByUsername.get(1);
        }

        // validate first file
        validateFileOnDisk(firstFileDataFromDb);
        Assert.assertNotEquals(sha1, firstFileDataFromDb.getSha1());
        Assert.assertNotEquals(fileUrl, firstFileDataFromDb.getUrl());
        Assert.assertEquals(username, firstFileDataFromDb.getUsername());
        // validate second file
        validateFileOnDisk(secondFileDataFromDb);
        Assert.assertEquals(sha1, secondFileDataFromDb.getSha1());
        Assert.assertEquals(fileUrl, secondFileDataFromDb.getUrl());
        Assert.assertEquals(username, secondFileDataFromDb.getUsername());
    }

    private String uploadFile(String fileName, String username) throws IOException, EmptyFileException, DbException {
        File file = GeneralUtils.getResource(this.getClass(), String.format("files/%s", fileName));
        FileInputStream fis = new FileInputStream(file);
        MockMultipartFile multipartFile = new MockMultipartFile("file", fileName, Files.probeContentType(file.toPath()), fis);
        String sha1String = DigestUtils.sha1Hex(multipartFile.getBytes());
        sha1Files.add(sha1String);

        return fileService.uploadFile(multipartFile, username);
    }

    private void validateFileOnDisk(FileData fileData) {
        File file = new File(fileData.getPath());
        Assert.assertTrue(file.exists());
    }

    private String getSha1FromUrl(String url) {
        return url.replace(interactionsConfig.getProperty("files.url"), "");
    }
}