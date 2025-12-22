package com.example.java.io.nio_stream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class SyncCopySample {

  public static void main(String[] args) {
    String srcPath = System.getProperty("user.dir") + "\\tmp\\test.txt";
    String dstPath = System.getProperty("user.dir") + "\\tmp\\dst.txt";

    try {
      FileChannel src = new FileInputStream(srcPath).getChannel();
      FileChannel dst = new FileOutputStream(dstPath).getChannel();
      dst.transferFrom(src, 0, src.size()); // zero-copy
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
