import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
  
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: java Client <hostname> <port>");
      return;
    }
    String hostName = args[0];
    int port = Integer.parseInt(args[1]);
    DatagramSocket socket = new DatagramSocket();
    byte[] buf = new byte[1024];
    InetAddress address = InetAddress.getByName(hostName);
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    String in = null;
    while ((in = reader.readLine()) != null) {
      System.out.println("(Client/<SEND>): " + in);
      buf = in.getBytes();
      DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
      socket.send(packet);
      packet = new DatagramPacket(buf, buf.length);
      socket.receive(packet);
      String received = new String(packet.getData(), 0, packet.getLength());
      System.out.println("(Client/<RECV>): " + received);
    }
    socket.close();
  }
}
