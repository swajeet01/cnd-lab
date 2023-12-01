package com.sg.client;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.sg.server.Server;
import com.sg.util.PeerEntry;

/*
 * File: src/com/sg/client/Client.java
 * Description: Client class for Assignment 8.
 * Author: Swajeet Swarnkar
 * M.Tech. IT NIT Raipur 2023-25
 * */
public class Client {
  public static final int SERVER_PORT = 16161;
  public static final int LISTNER_PORT = 16162;

  public static final String EXIT_MESSAGE = "bye!";
  public static final String TEMP_HS_RESPONSE = "200 OKAY";
  public static final String OPTIONS_MESSAGE = "Wait(W)/Initiate(I)/Quit(Q)? ";
  public static final String RESPONSE_WAIT = "W";
  public static final String RESPONSE_INITIATE = "I";
  public static final String RESPONSE_QUIT = "Q";


  public static class UnableToConnectException extends Exception {
    public UnableToConnectException(String message) {
      super(message);
    }
  }

  public static class BadAliasException extends Exception {
    public BadAliasException(String message) {
      super(message);
    }
  }

  public static class BadResponseFromServerException extends Exception {
    public BadResponseFromServerException(String message) {
      super(message);
    }
  }

  public static class BadPeerListException extends Exception {
    public BadPeerListException(String message) {
      super(message);
    }
  }

  public static class BadPeerAliasException extends Exception {
    public BadPeerAliasException(String message) {
      super(message);
    }
  }

  public static class HandshakeFailedException extends Exception {
    public HandshakeFailedException(String message) {
      super(message);
    }
  }

  private static Set<PeerEntry> registerAsPeer(String hostName, String alias)
      throws UnableToConnectException {
    try (
      Socket socket = new Socket(hostName, SERVER_PORT);
      PrintWriter toServer = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    ) {
      toServer.println(
        Server.COMMAND_ADD_CL + " " + alias
      );
      String response = fromServer.readLine();
      if (response == null || response.isBlank())
        throw new BadResponseFromServerException("Unable to receive response from server.");
      String peerStr = fromServer.readLine();
      if (peerStr == null || peerStr.isBlank())
        throw new BadPeerListException("Bad peer list received from server.");
      Set<PeerEntry> peers = Arrays.stream(peerStr.trim().split(":"))
        .map(PeerEntry::new)
        .collect(Collectors.toCollection(HashSet::new));
      return peers;
    } catch (
        BadResponseFromServerException |
        BadPeerListException |
        IllegalArgumentException |
        IOException e
    ) {
      throw new UnableToConnectException(e.getMessage());
    }
  }

  private static void resignAsPeer(String hostName, String alias) throws UnableToConnectException {
    try (
      Socket socket = new Socket(hostName, SERVER_PORT);
      PrintWriter toServer = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    ) {
      toServer.println(
        Server.COMMAND_REM_CL +
        " " + alias
      );
      String response = fromServer.readLine();
      if (response == null || response.isBlank())
        throw new BadResponseFromServerException("Unable to receive response from server.");
      /*
      String peerStr = fromServer.readLine();
      if (peerStr == null || peerStr.isBlank())
        throw new BadPeerListException("Bad peer list received from server.");
      Set<PeerEntry> peers = Arrays.stream(peerStr.trim().split(":"))
        .map(PeerEntry::new)
        .collect(Collectors.toCollection(HashSet::new));
      return peers;
      /* Ignore peer list */
    } catch (
        BadResponseFromServerException |
        IOException e
    ) {
      throw new UnableToConnectException(e.getMessage());
    }
  }

  private static Set<PeerEntry> fetchPeers(String hostName, String alias) throws UnableToConnectException {
    try (
      Socket socket = new Socket(hostName, SERVER_PORT);
      PrintWriter toServer = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    ) {
      toServer.println(
        Server.COMMAND_FETCH_CL + " " + alias
      );
      String response = fromServer.readLine();
      if (response == null || response.isBlank())
        throw new BadResponseFromServerException("Unable to receive response from server.");
      String peerStr = fromServer.readLine();
      if (peerStr == null || peerStr.isBlank())
        throw new BadPeerListException("Bad peer list received from server.");
      Set<PeerEntry> peers = Arrays.stream(peerStr.trim().split(":"))
        .map(PeerEntry::new)
        .collect(Collectors.toCollection(HashSet::new));
      return peers;
    } catch (
        BadResponseFromServerException |
        BadPeerListException |
        IllegalArgumentException |
        IOException e
    ) {
      throw new UnableToConnectException(e.getMessage());
    }
  }

  private static void printPeers(Set<PeerEntry> peers, String messageOnFail) throws BadPeerListException {
      System.out.println("Available peers:");
      if (peers == null) throw new BadPeerListException(messageOnFail);
      peers.forEach(peer -> System.out.println(peer));
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("No hostname provided.");
      return;
    }
    System.out.println("Ready.");

    String hostName = args[0];
    Console console = System.console();
    if (console == null) {
      System.err.println("No console found, exiting.");
      return;
    }
    String alias = console.readLine("Alias: ");
    if (alias == null || alias.isBlank()) {
      System.err.println("Bad alias, exiting.");
      return;
    }

    try {

      Set<PeerEntry> peers = null;
      peers = registerAsPeer(hostName, alias);
      printPeers(peers, "Unable to retrive peer list.");

      String action = null;
      while ((action = console.readLine(Client.OPTIONS_MESSAGE)) != null) {
        if (action.isBlank()) continue;
        if (RESPONSE_WAIT.equals(action)) {
          Thread t = new Thread(new IncommingConnectionHandler(alias, console));
          t.start();
          try { t.join(); } catch (InterruptedException e) {}
          peers = fetchPeers(hostName, alias);
          printPeers(peers, "Unable to fetch lastest peer list.");
        } else if (RESPONSE_INITIATE.equals(action)) {
          Thread t = new Thread(new OutgoingConnectionHandler(alias, console));
          t.start();
          try { t.join(); } catch (InterruptedException e) {}
          peers = fetchPeers(hostName, alias);
          printPeers(peers, "Unable to fetch lastest peer list.");
        } else if (RESPONSE_QUIT.equals(action)) {
          break;
        }
      }

      resignAsPeer(hostName, alias);
    } catch (BadPeerListException e) {
      System.err.println(e.getMessage());  
    } catch (UnableToConnectException e) {
      System.err.println(e.getMessage());
    }

    System.out.println("Done.");
  }
}

class MessageController {

  private BufferedReader reader;
  private PrintWriter writer;
  private Console console;
  private String alias;
  private String aliasFrom;
  private Thread messageReader;
  private Thread messageWriter;

  public MessageController(BufferedReader reader, PrintWriter writer, Console console, String alias, String aliasFrom) {
    this.reader = reader;
    this.writer = writer;
    this.console = console;
    this.alias = alias;
    this.aliasFrom = aliasFrom;
  }

  void finishedWriting() {
    if (messageReader == null || !messageReader.isAlive() || messageReader.isInterrupted())
      return;
    messageReader.interrupt();
  }

  void finishedReading() {
    if (messageWriter == null || !messageWriter.isAlive() || messageWriter.isInterrupted()) {
      return;
    }
    messageWriter.interrupt();
  }

  BufferedReader getReader() { return reader; }

  PrintWriter getWriter() { return writer; }

  Console getConsole() { return console; }

  String getAlias() { return alias; }

  String getAliasFrom() { return aliasFrom; }

  void execute() throws InterruptedException {
    messageReader = new Thread(new MessageReader(this));
    messageWriter = new Thread(new MessageWriter(this));
    messageReader.start();
    messageWriter.start();
    messageReader.join();
    messageWriter.join();
  }

}

class MessageReader implements Runnable {
  MessageController controller;
  BufferedReader reader = null;
  String aliasFrom = null;
  public MessageReader(MessageController controller) {
    this.controller = controller;
    this.reader = controller.getReader();
    this.aliasFrom = controller.getAliasFrom();
  }
  @Override
  public void run() {
    try {
      String message = null;
      while ((message = reader.readLine()) != null) {
        if (Thread.interrupted()) { break; }
        if (Client.EXIT_MESSAGE.equals(message)) {
          System.out.print("Connection has been terminated by peer, Press <Enter> to close connection.");
          break;
        }
        System.out.println(String.format("%s -from [%s]", message, aliasFrom));
      }
      controller.finishedReading();
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }
}

class MessageWriter implements Runnable {
  MessageController controller;
  PrintWriter writer;
  Console console;
  String alias;
  public MessageWriter(MessageController controller) {
    this.controller = controller;
    this.writer = controller.getWriter();
    this.console = controller.getConsole();
    this.alias = controller.getAlias();
  }
  @Override
  public void run() {
    String message = null;
    while ((message = console.readLine()) != null) {
      if (Thread.interrupted()) { break; }
      writer.println(message);
      if (Client.EXIT_MESSAGE.equals(message)) {
        System.out.println("Waiting for other peer to close connection.");
        break;
      }
    }
    controller.finishedWriting();
  }
}

class IncommingConnectionHandler implements Runnable {
  String alias = null;
  Console console;
  public IncommingConnectionHandler(String alias, Console console) {
    this.alias = alias;
    this.console = console;
  }

  String handShake(BufferedReader reader, PrintWriter writer) throws IOException, Client.BadPeerAliasException, Client.HandshakeFailedException {
    String peerAlias = reader.readLine();
    if (peerAlias == null || peerAlias.isBlank()) {
      throw new Client.BadPeerAliasException("Received bad alias.");
    }
    writer.println(alias);
    String handshakeResponse = reader.readLine();
    if (handshakeResponse == null || handshakeResponse.isBlank()) {
      throw new Client.HandshakeFailedException("Handshake failed.");
    }
    return peerAlias;
  }

  @Override
  public void run() {
    try (
      ServerSocket listnerSocket = new ServerSocket(Client.LISTNER_PORT);
      Socket connectionSocket = listnerSocket.accept();
      BufferedReader reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
      PrintWriter writer = new PrintWriter(connectionSocket.getOutputStream(), true);
    ) {
      String peerAlias = handShake(reader, writer);
      System.out.println("Connected with " + peerAlias);
      MessageController controller = new MessageController(reader, writer, console, alias, peerAlias);
      try {
        controller.execute();
      } catch (InterruptedException e) {}
    } catch (IOException e) {
      System.out.println(e.getMessage());
    } catch (Client.BadPeerAliasException e) {
      System.out.println(e.getMessage());
    } catch (Client.HandshakeFailedException e) {
      System.out.println(e.getMessage());
    }
  }
}

class OutgoingConnectionHandler implements Runnable {
  private Console console;
  private String alias;
  public OutgoingConnectionHandler(String alias, Console console) {
    this.console = console;
    this.alias = alias;
  }
  @Override
  public void run() {
    PeerEntry peer = null;
    String peerRepresentation = null;
    while ((peerRepresentation = console.readLine("Connect with? ")) != null) {
      if (peerRepresentation.isBlank()) continue;
      try {
        peer = new PeerEntry(peerRepresentation);
      } catch (IllegalArgumentException e) {
        System.err.println("Enter valid peer identifier.");
      }
      if (peer != null) break;
    }
    beginMessaging(peer.getIpAddr(), peer.getAlias());
  }

  void beginMessaging(String hostName, String peerAlias) {
    try (
      Socket connectionSocket = new Socket(hostName, Client.LISTNER_PORT);
      BufferedReader reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
      PrintWriter writer = new PrintWriter(connectionSocket.getOutputStream(), true);
    ) {
      String responsePeerAlias = handShake(reader, writer);
      System.out.println("Connected with " + responsePeerAlias);
      MessageController controller = new MessageController(reader, writer, console, alias, responsePeerAlias);
      try {
        controller.execute();
      } catch (InterruptedException e) {}
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } catch (Client.BadPeerAliasException e) {
      System.err.println(e.getMessage());
    }
  }

  String handShake(BufferedReader reader, PrintWriter writer) throws IOException, Client.BadPeerAliasException {
    writer.println(alias);
    String peerAlias = reader.readLine();
    if (peerAlias == null || peerAlias.isBlank()) {
      throw new Client.BadPeerAliasException("Received bad peer alias.");
    }
    writer.println(Client.TEMP_HS_RESPONSE);
    return peerAlias;
  }
}
