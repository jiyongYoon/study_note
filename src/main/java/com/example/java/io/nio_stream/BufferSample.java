package com.example.java.io.nio_stream;

import java.nio.IntBuffer;

public class BufferSample {

  public static void main(String[] args) {
    IntBuffer buffer = IntBuffer.allocate(100);// new byte[100] 처럼 공간 할당
    System.out.println("buffer.position(0) = " + buffer.position(0));

    for (int i = 0; i < 50; i++) {
      buffer.put((i + 1) * 10);
    }

    System.out.println("Position: " + buffer.position());

    System.out.println("buffer.get(0) = " + buffer.get(0));
    System.out.println("buffer.get(49) = " + buffer.get(49));
    System.out.println("buffer.position(25) = " + buffer.position(25));
    System.out.println("buffer.get(0) = " + buffer.get(0));

    /**
     * buffer.position(0) = java.nio.HeapIntBuffer[pos=0 lim=100 cap=100]
     * Position: 50
     * buffer.get(0) = 10
     * buffer.get(49) = 500
     * buffer.position(25) = java.nio.HeapIntBuffer[pos=25 lim=100 cap=100]
     * buffer.get(0) = 10
     */
  }

}
