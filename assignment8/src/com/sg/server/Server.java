package com.sg.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sg.util.PeerEntry;

/*
 * File: src/com/sg/server/Server.java
 * Description: Server class for Assignment 8.
 * Author: Swajeet Swarnkar
 * M.Tech. IT NIT Raipur 2023-25
 * */
public class Server {

  public static final int PORT = 16161;
  public static final int STATUS_UNKNOWN = 100;
  public static final int STATUS_OKAY = 101;
  public static final int STATUS_BAD_REQUEST = 102;
  public static final int STATUS_BAD_IP = 103;
  public static final int STATUS_INTERNAL_ERROR = 104;
  public static final int STATUS_UNKNOWN_REQUEST = 105;

  public static final String LOGGER_NAME = "DEFAULT_LOGGER";

  public static final String COMMAND_ADD_CL = "ADD";
  public static final String COMMAND_REM_CL = "REMOVE";
  public static final String COMMAND_FETCH_CL = "FETCH";

  public static class RepoUnableToHandleException extends Exception {
    public RepoUnableToHandleException(String message) {
      super(message);
    }
  }

  public static class BadIpException extends Exception {
    public BadIpException(String message) {
      super(message);
    }
  }

  public static class UnkownRequestException extends Exception {
    public UnkownRequestException(String message) {
      super(message);
    }
  }

  private static void dispatchHandlerThread(Socket socket, ServerRepo repo) {
    new Thread(new ServerRequestHandler(socket, repo)).start();
  }

  public static void log(String message) {
    Logger.getLogger(LOGGER_NAME).info(message);
  }
  
  public static void main(String[] args) {
    ServerRepo repo = new ServerRepo();
    try (
      ServerSocket welcomeSocket = new ServerSocket(PORT);
    ) {

      log("Server started on port: " + PORT);
      while (true) {
        Socket connectionSocket = welcomeSocket.accept();
        log("Received a new request, IP: " +
            connectionSocket.getInetAddress().toString() +
            ", dispatching handler.");
        dispatchHandlerThread(connectionSocket, repo);
      }

    } catch (IOException e) {
      System.err.println(e.getMessage());
    }

  }
}

class ServerRepo {

  private Set<PeerEntry> peers = new HashSet<>();

  synchronized void addClient(String ipAddr, String alias) throws Server.RepoUnableToHandleException {
    PeerEntry peer = new PeerEntry(ipAddr, alias);
    Server.log("Trying to add peer " + peer.toString());
    if (!peers.contains(peer) && !peers.add(peer)) {
      throw new Server.RepoUnableToHandleException("Can not add client.");
    } 
    Server.log("New peer added to peers " + peer.toString());
    Server.log("Repo: " + peers.toString());
  }

  synchronized void removeClient(String ipAddr, String alias) throws Server.RepoUnableToHandleException {
    PeerEntry peer = new PeerEntry(ipAddr, alias);
    Server.log("Trying to remove peer " + peer.toString());
    if (!peers.remove(peer)) {
      throw new Server.RepoUnableToHandleException("Can not remove client.");
    }
    Server.log("Peer removed from peers " + peer.toString());
    Server.log("Repo: " + peers.toString());
  }

  public String getPeerList() {
    return peers.stream()
    .map(PeerEntry::toString)
    .collect(Collectors.joining(":"));
  }

}

class ServerRequestHandler implements Runnable {

  private static final int COMMAND_IDX = 0;
  private static final int ALIAS_IDX = 1;

  private static final Map<Integer, String> STATUS_STRS = new HashMap<>();

  static {
    STATUS_STRS.put(Server.STATUS_UNKNOWN, "STATUS_UNKNOWN");
    STATUS_STRS.put(Server.STATUS_OKAY, "STATUS_OKAY");
    STATUS_STRS.put(Server.STATUS_BAD_REQUEST, "STATUS_BAD_REQUEST");
    STATUS_STRS.put(Server.STATUS_BAD_IP, "STATUS_BAD_IP");
    STATUS_STRS.put(Server.STATUS_INTERNAL_ERROR, "STATUS_INTERNAL_ERROR");
    STATUS_STRS.put(Server.STATUS_UNKNOWN_REQUEST, "STATUS_UNKNOWN_REQUEST");
  }

  private Socket clientSocket = null;
  private ServerRepo repo = null;

  public ServerRequestHandler(Socket clientSocket, ServerRepo repo) {
    this.clientSocket = clientSocket;
    this.repo = repo;
  }
  
  int executeCommand(String[] tokens) {
    int status = Server.STATUS_UNKNOWN;
    try {
      String command = tokens[COMMAND_IDX];
      String ipAddr = clientSocket.getInetAddress().toString();
      if (ipAddr == null || ipAddr.isBlank())
        throw new Server.BadIpException("Unable to get IP address from socket.");
      ipAddr = ipAddr.substring(1);
      if (Server.COMMAND_ADD_CL.equals(command)) {
        String alias = tokens[ALIAS_IDX];
        repo.addClient(ipAddr, alias);
      } else if (Server.COMMAND_REM_CL.equals(command)) {
        String alias = tokens[ALIAS_IDX];
        repo.removeClient(ipAddr, alias);
      } else if (Server.COMMAND_FETCH_CL.equals(command)) {
        status = Server.STATUS_OKAY;
      } else {
        throw new Server.UnkownRequestException("Unkown command received.");
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      Server.log("Received bad request from client.");
      status = Server.STATUS_BAD_REQUEST;
    } catch (Server.BadIpException e) {
      Server.log("Unable to get IP address from request.");
      status = Server.STATUS_BAD_IP;
    } catch (Server.RepoUnableToHandleException e) {
      Server.log("ServerRepo unable to handle request.");
      status = Server.STATUS_INTERNAL_ERROR;
    } catch (Server.UnkownRequestException e) {
      Server.log("Unkown request received.");
      status = Server.STATUS_UNKNOWN_REQUEST;
    }
    if (status == Server.STATUS_UNKNOWN) status = Server.STATUS_OKAY;
    return status;
  }

  @Override
  public void run() {
    try (
      BufferedReader reader = new BufferedReader(
        new InputStreamReader(clientSocket.getInputStream()));
      PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
    ) {
      String commandString = reader.readLine();
      Server.log("Request: " + commandString);
      String[] tokens = commandString.trim().split("\\s+");
      int status = executeCommand(tokens);
      // String response = String.format(RESPONSE_FORMAT, status, STATUS_STRS.get(status));
      String statusMsg = String.format("%d %s", status, STATUS_STRS.get(status));
      String peerList = repo.getPeerList();
      Server.log(statusMsg);
      writer.println(statusMsg);
      writer.println(peerList);
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } finally {
      if (clientSocket != null && !clientSocket.isClosed()) {
        try { clientSocket.close(); } catch (IOException ignore) {}
      }
    }
  }
  
}
