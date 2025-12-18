package com.example.java.io.io_stream;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class SystemOut {

  public static void main(String[] args) throws FileNotFoundException {
    System.out.println("Console: Hello world");
    String currentPath = System.getProperty("user.dir");

    PrintStream originalOut = System.out; // console out을 잠시 보관
    try (PrintStream ps = new PrintStream(currentPath + "\\tmp\\test.txt")) {
      System.setOut(ps); // 파일 출력을 System.out으로 세팅
      System.out.println("test.txt: Hello world");
    }
    System.setOut(originalOut); // 다시 콘솔 출력으로 세팅
    System.out.println("Console: Bye world");
  }

  /**
   * 콘솔에는
   * Console: Hello world
   * Console: Bye world
   * 두 개만 출력된다.
   */

}
