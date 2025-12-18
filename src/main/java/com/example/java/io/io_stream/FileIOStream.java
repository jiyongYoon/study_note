package com.example.java.io.io_stream;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serial;
import java.io.Serializable;
import java.io.Writer;

public class FileIOStream {

  public static void main(String[] args) throws IOException {
    String userDir = System.getProperty("user.dir");
    String tmpDirPath = userDir + "\\tmp";
    String filePath = tmpDirPath + "\\test.txt";
    String contents = "ABCDE";

    File tmpDir = new File(tmpDirPath);

    if (!tmpDir.exists()) {
      tmpDir.mkdirs();
    }

    writeByByte(filePath);
    writeByString(filePath, contents);

    read(filePath);
    readWithBuffer(filePath);
    readWithBufferedReader(filePath);
    readFromDataStream(filePath);

    /////////////////////////////////////////////////////

    if (writeObject(new MyDataObject(10, "Test Data"), filePath)) {
      System.out.println("success!");
    } else {
      System.out.println("failed!");
    }

    MyDataObject myDataObject = readObject(filePath);
    if (myDataObject != null) {
      System.out.println(myDataObject);
    } else {
      System.out.println("Error: Failed to read MyDataObject!");
    }

  }

  private static void writeByByte(String filePath) throws IOException {
    try (OutputStream os = new FileOutputStream(filePath)) {
      os.write(new byte[]{65, 66, 67, 68, 69});
    }
  }

  /**
   * String으로 File Input을 지원하는 확장 클래스
   */
  private static void writeByString(String filePath, String contents) throws IOException {
    try (Writer writer = new FileWriter(filePath)) {
      writer.write(contents);
    }
  }

  /**
   * FileReader(or FileInputStream)을 통해 데이터를 읽어옴
   */
  private static void read(String filePath) throws IOException {
    int readCount = 0;
    try (Reader reader = new FileReader(filePath)) { // 문자 스트림
//    try (InputStream reader = new FileInputStream(filePath)) { // byte 스트림
      int data;
      while ((data = reader.read()) != -1) {
        System.out.print((char) data);
        readCount++;
      }
    }
    System.out.println("\nreader.read() - total readCount = " + readCount);
  }
  /**
   * ABCDE
   * reader.read() - total readCount = 5
   */

  /**
   * read 할 때 메모리 버퍼를 만들어서 여러 단어를 읽어옴
   */
  private static void readWithBuffer(String filePath) throws IOException {
    int readCount = 0;
    try (Reader reader = new FileReader(filePath)) {
      char[] buffer = new char[3];
      int data;
      while ((data = reader.read(buffer)) != -1) {
        System.out.print(new String(buffer, 0, data));
        readCount++;
      }
    }
    System.out.println("\nreader.read(buffer) - total readCount = " + readCount);
  }
  /**
   * ABCDE
   * reader.read(buffer) - total readCount = 2 --> 버퍼 크기가 3이기 때문에 3개 / 2개 읽음
   */

  /** 보조스트림 **/

  /**
   * Java의 BufferedReader를 사용해 한 줄씩 버퍼로 읽어옴
   */
  private static void readWithBufferedReader(String filePath) throws IOException {
    int readCount = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = br.readLine()) != null) {
        System.out.println(line);
        readCount++;
      }
    }
    System.out.println("BufferedReader - total readCount = " + readCount);
  }
  /**
   * ABCDE
   * BufferedReader - total readCount = 1
   */

  private static void readFromDataStream(String filePath) throws IOException {
    int readCount = 0;
    try (DataInputStream dis = new DataInputStream(new FileInputStream(filePath))) {
      int data;
      while (true) {
        try {
          data = dis.readByte(); // 1 byte씩 읽음
          System.out.print((char) data);
          readCount++;
        } catch (EOFException e) {
          break;
        }
      }
    }
    System.out.println("\nDataInputStream.readByte() & (char) casting - total readCount = " + readCount);
  }
  /**
   * ABCDE
   * DataInputStream.readByte() & (char) casting - total readCount = 5
   */


  static class MyDataObject implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int type = 0;
    private final String data;

    public MyDataObject(int type, String data) {
      this.type = type;
      this.data = data;
    }

    public int getType() {
      return type;
    }

    public void setType(int type) {
      this.type = type;
    }

    public String getData() {
      return data;
    }

    @Override
    public String toString() {
      return
          "MyDataObjectVer=" + serialVersionUID + "\n" +
              "MyDataObject{" +
              "type=" + type +
              ", data='" + data + '\'' +
              '}';
    }
  }

  public static MyDataObject readObject(String filePath) {
    MyDataObject dto;
    try {
      InputStream is = new FileInputStream(filePath);
      ObjectInputStream ois = new ObjectInputStream(is);
      dto = (MyDataObject) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      return null;
    }
    return dto;
  }

  public static boolean writeObject(MyDataObject dto, String filePath) {
    try {
      OutputStream os = new FileOutputStream(filePath);
      ObjectOutputStream oos = new ObjectOutputStream(os);
      oos.writeObject(dto);
    } catch (IOException e) {
      return false;
    }
    return true;
  }
}
