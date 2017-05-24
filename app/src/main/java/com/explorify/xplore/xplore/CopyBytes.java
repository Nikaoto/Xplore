package com.explorify.xplore.xplore;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by nikao on 5/24/2017.
 * This class is meant for copying data byte by byte
 */

class CopyBytes {
    private void transferBytes(InputStream input, OutputStream output) throws IOException {
        //Creating byte array for data copying
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) > 0)
            output.write(buffer, 0, length);

        //Finalizing data transfer
        output.flush();
        output.close();
        input.close();
    }

    CopyBytes(InputStream input, OutputStream output) throws IOException {
        transferBytes(input, output);
    }

    //when input is stream and output is string
    CopyBytes(InputStream input, String outputPath) throws IOException {
        new CopyBytes(input, new FileOutputStream(outputPath));
    }

    //when input is string and output is stream
    CopyBytes(String inputPath, OutputStream output) throws IOException {
        new CopyBytes(new FileInputStream(inputPath), output);
    }

    //when input and output are both strings
    CopyBytes(String inputPath, String outputPath) throws IOException {
        new CopyBytes(new FileInputStream(inputPath), new FileOutputStream(outputPath));
    }
}
