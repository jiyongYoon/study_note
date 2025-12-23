package com.example.java.io.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class EchoServer {

  static class ClientHandler implements Runnable {
    Socket clientSocket = null;
    BufferedReader reader = null;
    PrintWriter writer = null;

    public ClientHandler(Socket clientSocket) {
      this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
      try {
        InputStream inputStream = clientSocket.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream));

        OutputStream outputStream = clientSocket.getOutputStream();
        writer = new PrintWriter(outputStream, true);

        String msg;
        while ((msg = reader.readLine()) != null) {
          if ("exit".equalsIgnoreCase(msg)) {
            break;
          }

          System.out.println("Received from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + ": " + msg);
          writer.println(msg); // echo
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          if (clientSocket != null) clientSocket.close();
          if (reader != null) reader.close();
          if (writer != null) writer.close();
          System.out.println("Disconnected: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
        } catch (IOException e) {
          System.out.println(e.getMessage());
        }
      }
    }
  }

  public static void main(String[] args) {
    ServerSocket serverSocket = null;
    int listenPort = 20000;

    try {
      System.out.println("Server start...");
      serverSocket = new ServerSocket(listenPort);

      while (true) {
        Socket clientSocket = serverSocket.accept(); // block
        System.out.println("New client connected...");
        System.out.println("[" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "]");
        ClientHandler handler = new ClientHandler(clientSocket);
        Thread thread = new Thread(handler);
        thread.start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (serverSocket != null) serverSocket.close();
        System.out.println("Server stop...");
      } catch (IOException e) {
        System.out.println(e.getMessage());
      }
    }
  }

}
