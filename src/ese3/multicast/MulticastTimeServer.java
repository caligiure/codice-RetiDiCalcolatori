package ese3.multicast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Date;

public class MulticastTimeServer {
	public static void main(String[] args) {
		MulticastSocket socket = null;
		try {
			socket = new MulticastSocket(3575);
			int n = 1;
			while (n <= 100) {
				byte[] buf = new byte[256];
				// non aspetta la richiesta
				String dString = new Date().toString();
				buf = dString.getBytes();
				// invia il messaggio in broadcast
				InetAddress group = InetAddress.getByName("230.0.0.1");
				DatagramPacket packet = new DatagramPacket(buf, buf.length, group, 3575);
				socket.send(packet);
				System.out.println("Broadcasting: " + dString);
				Thread.sleep(1000);
				n++;
			}
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
			socket.close();
		}
	}
}