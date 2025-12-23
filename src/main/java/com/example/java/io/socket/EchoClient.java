package com.example.java.io.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class EchoClient {

  public static void main(String[] args) {
    BufferedReader consoleInput = null;
    Socket socket = null;
    PrintWriter writer = null;
    BufferedReader reader = null;

    try {
      consoleInput = new BufferedReader(new InputStreamReader(System.in));

      socket = new Socket("127.0.0.1", 20000);
      System.out.println("## connected to server ##");

      OutputStream output = socket.getOutputStream();
      writer = new PrintWriter(output, true);
      InputStream input = socket.getInputStream();
      reader = new BufferedReader(new InputStreamReader(input));

      String msg;
      while ((msg = consoleInput.readLine()) != null) {
        if ("exit".equalsIgnoreCase(msg)) {
          break;
        }

        writer.println(msg); // send
        String response = reader.readLine();
        System.out.println("From server: " + response);
      }
    } catch (IOException e) {
      System.out.println(e.getMessage());
    } finally {
      try {
        if (socket != null) socket.close();
        if (consoleInput != null) consoleInput.close();
        if (reader != null) reader.close();
        if (writer != null) writer.close();
      } catch (IOException e) {
        System.out.println(e.getMessage());
      }
    }
  }

}
