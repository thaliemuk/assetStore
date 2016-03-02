package org.t2k.interactions.services.files;

import org.springframework.web.multipart.MultipartFile;
import org.t2k.interactions.models.FileData;
import org.t2k.interactions.models.exceptions.DbException;
import org.t2k.interactions.models.exceptions.EmptyFileException;

import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 23/07/2015
 * Time: 08:50
 */
public interface FileService {

    List<FileData> getFileDataListBySha1(String sha1) throws DbException;

    List<FileData> getFileDataListByUsername(String username) throws DbException;

    String uploadFile(MultipartFile file, String username) throws EmptyFileException, IOException, DbException;

    void deleteFileFromDiskAndItsRelevantFileDataFromDb(FileData fileData) throws IOException, DbException;

    void deleteFileDataFromDbBySha1(String sha1) throws DbException;
}