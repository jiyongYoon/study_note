package com.example.java.file_IO;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileIOTest {

  public static void main(String[] args) throws IOException {
    // Case 1: BufferedOutputStream 없음
    long startAt = System.currentTimeMillis();
    FileOutputStream fos = new FileOutputStream("test1.txt");
    for (int i = 0; i < 1024 * 1024; i++) {
      fos.write(65);  // 1,048,576번의 시스템 콜
    }
    fos.close();
    long endAt = System.currentTimeMillis();
    System.out.println("1: BufferedOutputStream 없음 - 총 시간: " + (endAt - startAt) + "ms");
    // 1: BufferedOutputStream 없음 - 총 시간: 1459ms

    // Case 2: BufferedOutputStream 사용
    startAt = System.currentTimeMillis();
    FileOutputStream fos2 = new FileOutputStream("test2.txt");
    BufferedOutputStream bos = new BufferedOutputStream(fos2);
    for (int i = 0; i < 1024 * 1024; i++) {
      bos.write(65);  // Java 메모리 복사만
    }
    bos.close();  // 약 128번의 시스템 콜 (1MB / 8KB)
    endAt = System.currentTimeMillis();
    System.out.println("2: BufferedOutputStream 사용 - 총 시간: " + (endAt - startAt) + "ms");
    // 2: BufferedOutputStream 사용 - 총 시간: 6ms
  }

}
