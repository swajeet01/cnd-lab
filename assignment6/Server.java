import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Server {
  
  public static class ServerThread implements Runnable {
    DatagramSocket socket = null;

    public ServerThread(int port) throws Exception {
      socket = new DatagramSocket(port);
    }

    @Override
    public void run() {
      while (true) {
        try {
          byte[] buf = new byte[1024];
          DatagramPacket packet = new DatagramPacket(buf, buf.length);
          socket.receive(packet);

          String received = new String(packet.getData(), 0, packet.getLength());
          System.out.println("(Server/<RECV>): " + received);
          String reversed = new StringBuilder(received).reverse().toString();
          buf = reversed.getBytes();
          System.out.println("(Server/<SEND>): " + reversed);
          
          InetAddress address = packet.getAddress();
          int port = packet.getPort();

          packet = new DatagramPacket(buf, buf.length, address, port);
          socket.send(packet);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Usage: java Server <port>");
      return;
    }
    int port = Integer.parseInt(args[0]);
    new Thread(new ServerThread(port)).start();
  }
}
