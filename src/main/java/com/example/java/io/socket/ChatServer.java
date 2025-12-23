package com.example.java.io.socket;

import com.example.java.io.socket.EchoServer.ClientHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

  static class ClientHandler implements Runnable {
    private Socket clientSocket = null;
    private BufferedReader reader = null;
    private PrintWriter writer = null;

    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    public ClientHandler(Socket clientSocket) {
      this.clientSocket = clientSocket;
      clients.add(this);
    }

    private void sendMessageAll(String msg, InetAddress senderInetAddress, int senderPort) {
      for (ClientHandler client : clients) {
        if (client.clientSocket.getInetAddress().getHostAddress().equals(senderInetAddress.getHostAddress())
        && client.clientSocket.getPort() == senderPort) {
          continue;
        }
        client.writer.println(senderInetAddress + ":" + senderPort + ": " + msg);
      }
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
          sendMessageAll(msg, clientSocket.getInetAddress(), clientSocket.getPort());
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
        Socket clientSocket = serverSocket.accept();
        System.out.println("New client connected...");
        System.out.println("[" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "]");
        ClientHandler handler = new ClientHandler(clientSocket);
        Thread thread = new Thread(handler);
        thread.start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
