package com.example.java.io.nio_stream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class MmfSample {

  private static final String filePath = System.getProperty("user.dir") + "\\tmp\\test.txt";

  private static void writeData(String data) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(filePath);
        FileChannel channel = fos.getChannel()) {
      ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
      channel.write(buffer);
    }
  }

  public static void main(String[] args) throws IOException {
    writeData("log test: memory mapped file sample");
    try (RandomAccessFile file = new RandomAccessFile(filePath, "rw");
        FileChannel channel = file.getChannel()) {
      long size = channel.size();
      MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, size); // JVM 가상 주소 공간에 파일을 매핑함. JVM 힙에 데이터를 가져오지 않음!

      // 이 아래 코드는 Heap 메모리로 복사되는 코드라 zero-copy는 깨짐
      // MMF의 진짜 강점은 Heap 메모리 복사 없이 바로 네트워크로 전송하거나 파일 복사를 하는 경우에 생김
      byte[] data = new byte[(int) size];
      buffer.get(data); // 매핑된 가상주소에 접근하여 데이터 가져옴 - Page Cache에 없는 경우 OS가 디스크에서 페이지 로드 후 가져옴
      System.out.println(new String(data));
    }
  }
}
