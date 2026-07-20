package com.duoc.transportmanagement.controller;

import com.duoc.transportmanagement.dto.AssetDTO;
import com.duoc.transportmanagement.model.Asset;
import com.duoc.transportmanagement.service.AwsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/s3")
public class S3Controller {
    private AwsService awsService;

    @Autowired
    public S3Controller(AwsService awsService) {
        this.awsService = awsService;
    }

    @GetMapping("/getS3FileContent")
    public ResponseEntity<String> getS3FileContent(@RequestParam(value = "bucketName") String bucketName, @RequestParam(value = "fileName") String fileName) throws IOException {
        return new ResponseEntity<>(awsService.getS3FileContent(bucketName, fileName),  HttpStatus.OK);
    }

    @GetMapping("/listS3Files")
    public ResponseEntity<List<AssetDTO>> getS3Files(
            @RequestParam(value = "bucketName") String bucketName) {

        List<AssetDTO> list = new ArrayList<>();
        HttpStatus status = HttpStatus.OK;

        try {
            list = awsService.getS3Files(bucketName);
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(list, status);
    }

    @GetMapping("/downloadS3File")
    public ResponseEntity<ByteArrayResource> downloadS3File(@RequestParam(value = "bucketName") String bucketName, @RequestParam(value = "filePath") String filePath, @RequestParam(value = "fileName") String fileName) throws IOException {
        String key = filePath + fileName;

        byte[] data = awsService.downloadFile(bucketName, key);
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @DeleteMapping("/deleteObject")
    public ResponseEntity<String> deleteFile(@RequestParam(value = "bucketName") String bucketName, @RequestParam(value = "fileKey") String fileKey){
        awsService.deleteObject(bucketName,fileKey);
        return new ResponseEntity<>("File deleted",  HttpStatus.OK);
    }

    @GetMapping("/moveFile")
    public ResponseEntity<String> moveFile(@RequestParam(value = "bucketName") String bucketName,
                                           @RequestParam(value = "fileName") String fileKey,
                                           @RequestParam(value = "fileNameDest") String fileKeyDest){
        awsService.moveObject(bucketName,fileKey,fileKeyDest);
        return new ResponseEntity<>("File moved",  HttpStatus.OK);
    }

    @PostMapping("/uploadFile")
    public ResponseEntity<String> uploadFile(@RequestParam(value = "bucketName") String bucketName, @RequestParam(value = "filePath") String filePath, @RequestParam(value = "file")MultipartFile file){
        return  new ResponseEntity<>(awsService.uploadFile(bucketName, filePath, file),  HttpStatus.OK);
    }
}
