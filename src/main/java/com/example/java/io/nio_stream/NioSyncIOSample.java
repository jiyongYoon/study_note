package com.example.java.io.nio_stream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

/**
 * File -> (I/O) Stream -> (I/O) Channel <br>
 * FileChannel: 랜덤 액세스 지원, MMF(메모리 매핑 파일 - 파일을 메모리로 추상화) 지원 <br>
 * `singleChannel.position(singleChannel.position() - length);` 해당 코드를 보면 <br>
 * 파일을 마치 메모리와 같이 위치에 직접 랜덤 엑세스해서 읽는 형태임. <br>
 */
public class NioSyncIOSample {

  private static final String filePath = System.getProperty("user.dir") + "\\tmp\\test.txt";
  private static FileOutputStream fos;
  private static FileInputStream fis;
  private static FileChannel writeChannel;
  private static FileChannel readChannel;
  private static FileChannel singleChannel;

  private static void initChannels() throws IOException {
    fos = new FileOutputStream(filePath);
    writeChannel = fos.getChannel();
    fis = new FileInputStream(filePath);
    readChannel = fis.getChannel();

    singleChannel = FileChannel.open(Paths.get(filePath), // 파일 경로를 Path 객체로 변환 (NIO는 Path 기반)
        StandardOpenOption.TRUNCATE_EXISTING, // 기존 파일이 있으면 내용을 0바이트로 잘라냄
        StandardOpenOption.CREATE, // 파일이 없으면 새로 생성
        StandardOpenOption.READ, // 읽기 모드 허용 (channel.read 가능)
        StandardOpenOption.WRITE); // 쓰기 모드 허용 (channel.write 가능)
  }

  private static void readData() throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(256);
    int length = readChannel.read(buffer); // 파일 채널 -> 버퍼로 데이터 읽기 -> 읽은 바이트 수 반환이 되며, 읽은 만큼 channel position 자동 증가!
    if (length > 0) {
      byte[] data = new byte[length];
      buffer.get(0, data); // 버퍼의 0번 인덱스부터 length 바이트를 배열로 복사
      System.out.println("readData() = " + new String(data));
    }
  }

  private static void readDataAtSingleChannel(int length) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(256);
    if (singleChannel.read(buffer) > 0) {
      byte[] data = new byte[length];
      buffer.get(0, data);
      System.out.println("readDataAtSingleChannel() = " + new String(data));
    }
  }

  private static void writeData(String data) throws IOException {
    ByteBuffer buffer = ByteBuffer.wrap(data.getBytes()); // data.getBytes() 를 감싸는 ByteBuffer 생성 - 복사 없이 그대로 byte[] 사용
    writeChannel.write(buffer);
  }

  private static int writeDataAtSingleChannel(String data) throws IOException {
    ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
    return singleChannel.write(buffer);
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    try {
      initChannels();
      for (String input = scanner.nextLine(); !input.equals("exit"); input = scanner.nextLine()) {
        System.out.println("--- I/O 채널 각각 ---");
        writeData(input);
        readData();

        System.out.println("--- I/O 채널 통합 ---");
        int length = writeDataAtSingleChannel(input);
        singleChannel.position(singleChannel.position() - length); // 데이터를 쓴만큼 position을 앞으로 돌림 - 단일 채널이라서
        readDataAtSingleChannel(length);
      }
      readChannel.close();
      writeChannel.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
