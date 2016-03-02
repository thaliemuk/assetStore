package org.t2k.interactions.services.files;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.t2k.interactions.dal.DB;
import org.t2k.interactions.dal.Design;
import org.t2k.interactions.dal.View;
import org.t2k.interactions.models.FileData;
import org.t2k.interactions.models.exceptions.DbException;
import org.t2k.interactions.models.exceptions.EmptyFileException;
import org.t2k.interactions.utils.InteractionsConfig;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 23/07/2015
 * Time: 08:49
 */
public class FileServiceImpl implements FileService {

    private Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    private InteractionsConfig interactionsConfig;

    private void init() {
        File uploadedFilesDir = new File(interactionsConfig.getProperty("uploaded.files.dir"));
        if (!uploadedFilesDir.exists()) {
            uploadedFilesDir.mkdirs();
        }
    }

    @Override
     public List<FileData> getFileDataListBySha1(String sha1) throws DbException {
        logger.debug(String.format("Getting a list of file data by sha1: %s from DB.", sha1));
        return DB.findByView(Design.FILE_DATA, View.BY_SHA_1, FileData.class, sha1);
    }

    @Override
    public List<FileData> getFileDataListByUsername(String username) throws DbException {
        logger.debug(String.format("Getting a list of file data by username: %s from DB.", username));
        return DB.findByView(Design.FILE_DATA, View.BY_USERNAME, FileData.class, username);
    }

    /**
     * Upload a file to Interactions server. This brakes into 3 cases:
     * 1. File is being uploaded for the first time - the file will be saved to disk and a new FileData will be saved in DB under the username which uploaded it.
     * 2. File is being uploaded for the second time by the same user - nothing to be done beacause the file already was saved to both disk and DB.
     * 3. File is being uploaded for the second time by a different user - a new FileData will be saved in DB under the username of this user.
     * @param multipartFile - the uploaded file.
     * @param username - the username of the user who uploads the file.
     * @return the FileData object which contains all the data about the file.
     * @throws EmptyFileException - in case the file is empty.
     * @throws IOException - if failed to write/read the file to/from FS.
     * @throws DbException - if failed to write/read to/from the DB.
     */
    @Override
    public String uploadFile(MultipartFile multipartFile, String username) throws EmptyFileException, IOException, DbException {
        logger.debug(String.format("About to upload file: %s, size: %d, received by user: %s", multipartFile.getName(), multipartFile.getSize(), username));
        if (multipartFile.isEmpty()) {
            String errorMsg = String.format("The uploaded file \"%s\" is empty.", multipartFile.getName());
            throw new EmptyFileException(errorMsg);
        }

        byte[] bytes = multipartFile.getBytes();
        String sha1String = DigestUtils.sha1Hex(bytes);

        // Get the list of FileData (files descriptors) stored in DB, that their SHA1 matches the one of the uploaded file
        List<FileData> fileDataList = getFileDataListBySha1(sha1String);

        // Get the file data that matches the current user's username
        FileData fileDataForUsername = getFileDataForUsername(username, fileDataList);
        if (fileDataForUsername != null) {  // If the current user already have filedata for this file,
                                            // it means the file also already exists on disk, so just return the file data.
            logger.debug(String.format("The user already uploaded this file with sha1: %s to %s - no need to upload it again.", fileDataForUsername.getSha1(), fileDataForUsername.getPath()));
            return fileDataForUsername.getUrl();
        }

        // The user never uploaded this file
        File file;
        if (fileDataList.isEmpty()) { // If the file data list is empty, it means that no one ever uploaded this file before, so save it to disk
            file = new File(interactionsConfig.getProperty("uploaded.files.dir"), sha1String);
            saveFileToDisk(bytes, file);
            logger.debug(String.format("Saved file with sha1: %s to disk at: %s.", sha1String, file.getAbsoluteFile()));
        } else { // The file was already uploaded by other user(s), so take its paths from another user's file data
            file = new File(fileDataList.get(0).getPath());
            logger.debug(String.format("File with sha1: %s already exists on disk at: %s.", sha1String, file.getAbsoluteFile()));
        }

        // Save the current user's file data in DB and return it
        FileData fileData = saveFileDataInDb(multipartFile, file, username);
        logger.debug(String.format("Upload file completed. Saved file data to DB: %s", fileData));
        return fileData.getUrl();
    }

    @Override
    public void deleteFileFromDiskAndItsRelevantFileDataFromDb(FileData fileData) throws IOException, DbException {
        logger.debug(String.format("About to delete file %s from disk and all its relevant file data from DB.", fileData));
        try {
            FileUtils.forceDelete(new File(fileData.getPath()));
        } catch (IOException e) {
            logger.error(String.format("Failed to delete file %s from disk.", fileData.getPath()));
            throw e;
        }

        logger.debug(String.format("File %s was successfully removed from disk.", fileData.getPath()));
        deleteFileDataFromDbBySha1(fileData.getSha1());
    }

    @Override
    public void deleteFileDataFromDbBySha1(String sha1) throws DbException {
        logger.debug(String.format("About to delete all file data which match sha1 %s from DB.", sha1));
        DB.deleteByView(Design.FILE_DATA, View.BY_SHA_1, FileData.class, sha1);
        logger.debug(String.format("File with sha1 %s was successfully removed from DB.", sha1));
    }

    private FileData saveFileDataInDb(MultipartFile multipartFile, File file, String username) throws DbException {
        FileData fileData = new FileData(username, file, interactionsConfig.getProperty("files.url"), multipartFile.getOriginalFilename(), multipartFile.getContentType());
        try {
            return DB.save(fileData);
        } catch (DbException e) {
            logger.error(String.format("Failed to save the new uploaded file data to DB: %s", fileData), e);
            throw e;
        }
    }

    private void saveFileToDisk(byte[] bytes, File file) throws DbException, IOException {
        BufferedOutputStream stream = null;
        try {
            stream = new BufferedOutputStream(new FileOutputStream(file));
            stream.write(bytes);
        } catch(IOException e) {
            logger.error(String.format("Failed to close the output stream after writing file %s to disk.", file.getAbsolutePath()), e);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private FileData getFileDataForUsername(String username, List<FileData> fileDataList) {
        return fileDataList.stream().filter(fileData -> fileData.getUsername().equals(username)).findFirst().orElse(null);
    }
}