package com.xplore.database

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by nikao on 5/24/2017.
 * This class is meant for copying data byte by byte
 */

//A Kotlin object (singleton) that handles the copying with mulitple different arguments
object CopyBytes {
    @Throws(IOException::class)
    fun copy(input: InputStream, output: OutputStream) {

        //Creating byte array
        val buffer = ByteArray(1024)
        var length = input.read(buffer)

        //Transferring data
        while(length != -1) {
            output.write(buffer, 0, length)
            length = input.read(buffer)
        }

        //Finalizing
        output.flush()
        output.close()
        input.close()
    }

    //In - String, Out - Stream
    fun copy(input: String, output: OutputStream) {
        copy(FileInputStream(input), output)
    }

    //In - Stream, Out - String
    fun copy(input: InputStream, output: String) {
        copy(input, FileOutputStream(output))
    }

    //In - String, Out - String
    fun copy(input: String, output: String) {
        copy(FileInputStream(input), FileOutputStream(output))
    }
}
