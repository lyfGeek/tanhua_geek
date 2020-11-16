package com.tanhua.server;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestFastDFS {

    @Autowired
    protected FastFileStorageClient fastFileStorageClient;

    @Test
    public void testUpload() {
        String path = "C:\\Users\\geek\\Desktop\\png.。\\无标题·.png";
        File file = new File(path);

        try {
            StorePath storePath = this.fastFileStorageClient.uploadFile(FileUtils.openInputStream(file), file.length(), "jpg", null);

            System.out.println(storePath);// StorePath [group=group1, path=M00/00/00/wKghgF9sBBSAbsyvAELG45c6F4o645.jpg]
            System.out.println(storePath.getFullPath());// group1/M00/00/00/wKghgF9sBBSAbsyvAELG45c6F4o645.jpg
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
