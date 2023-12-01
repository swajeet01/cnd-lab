import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Server {

  public static class ServerRunnable implements Runnable {
    private Socket clientSocket;

    public ServerRunnable(Socket clientSocket) {
      this.clientSocket = clientSocket;
    }
    
    String getSortedDataAsString(String in) {
      return Arrays.stream(in.split("\\s+"))
        .mapToDouble(Double::parseDouble)
        .sorted()
        .mapToObj(Double::toString)
        .collect(Collectors.joining(" "));
    }
    @Override
    public void run() {
      try (
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
      ) {
        String fromClient = null;
        while ((fromClient = reader.readLine()) != null) {
          System.out.println("[Sever]: <RECV> " + fromClient);
          String response = getSortedDataAsString(fromClient);
          System.out.println("[Sever]: <SEND> " + response);
          writer.println(response);
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        try {
          clientSocket.close();
        } catch (Exception ignore) {}
      }
    }
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Usage: java Server <port>");
      return;
    }
    int port = Integer.parseInt(args[0]);
    try (
      ServerSocket serverSocket = new ServerSocket(port);
    ) {
      while (true) {
        Socket socket = serverSocket.accept();
        Thread t = new Thread(new ServerRunnable(socket));
        t.start();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
