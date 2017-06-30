package com.xplore

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by nikao on 5/24/2017.
 * This class is meant for copying data byte by byte
 */

internal class CopyBytes {
    @Throws(IOException::class)
    private fun transferBytes(input: InputStream, output: OutputStream) {
        //Creating byte array for data copying
        val buffer = ByteArray(1024)
        var length: Int

        //Transferring data
        do {
            length = input.read(buffer)
            if(length < 0) break
            output.write(buffer, 0, length)
        }
        while (length > 0)

        //Finalizing data transfer
        output.flush()
        output.close()
        input.close()
    }

    @Throws(IOException::class)
    constructor(input: InputStream, output: OutputStream) {
        transferBytes(input, output)
    }

    //when input is stream and output is string
    @Throws(IOException::class)
    constructor(input: InputStream, outputPath: String) {
        CopyBytes(input, FileOutputStream(outputPath))
    }

    //when input is string and output is stream
    @Throws(IOException::class)
    constructor(inputPath: String, output: OutputStream) {
        CopyBytes(FileInputStream(inputPath), output)
    }

    //when input and output are both strings
    @Throws(IOException::class)
    constructor(inputPath: String, outputPath: String) {
        CopyBytes(FileInputStream(inputPath), FileOutputStream(outputPath))
    }
}
