
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Usage: java Client <hostname> <port>");
      return;
    }
    String hostName = args[0];
    int port = Integer.parseInt(args[1]);
    try (
      Socket clientSocket = new Socket(hostName, port);
      BufferedReader fromKeyboard = new BufferedReader(new InputStreamReader(System.in));
      BufferedReader fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      PrintWriter toServer = new PrintWriter(clientSocket.getOutputStream(), true);
    ) {
      System.out.println("Ready.");
      String input = null;
      while ((input = fromKeyboard.readLine()) != null) {
        System.out.println("(Client/<SEND>): " + input);
        toServer.println(input);
        String output = fromServer.readLine();
        System.out.println("(Client/<RECV>): " + output);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
