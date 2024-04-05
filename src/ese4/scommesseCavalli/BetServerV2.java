package ese4.scommesseCavalli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.LinkedList;

public class BetServerV2 {
	private Hashtable<Integer, LinkedList<Scommessa>> scommesse;
	private Calendar limite;
	private AnalizzaScommesse tAnalizzaScommesse;
	private int sPort;

	public BetServerV2(int port, Calendar deadline) {
		scommesse = new Hashtable<Integer, LinkedList<Scommessa>>();
		for (int i = 1; i <= 12; i++)
			scommesse.put(i, new LinkedList<Scommessa>());
		limite = deadline;
		this.sPort = port;
		tAnalizzaScommesse = new AnalizzaScommesse();
		tAnalizzaScommesse.start();

	}// costruttore

	private class AnalizzaScommesse extends Thread {
		ServerSocket serv;

		public AnalizzaScommesse() {
			try {
				serv = new ServerSocket(sPort);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {

			while (true) {
				try {
					Socket socket = serv.accept();
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					Calendar now = Calendar.getInstance();
					if (now.before(limite)) {
						// riceve la scommessa (nel
						// formato("numcavallo puntata") e la inserisce nella
						// lista scommesse
						String line = in.readLine();
						int pos = line.indexOf(" ");
						int numCavallo = Integer.parseInt(line.substring(0, pos));
						long puntata = Long.parseLong(line.substring(pos + 1));
						InetAddress ip = socket.getInetAddress();
						Scommessa scommessa = new Scommessa(numCavallo, puntata, ip);
						int key = scommessa.getID();

						LinkedList<Scommessa> list = new LinkedList<Scommessa>();
						list.add(scommessa);
						scommesse.put(key, list);

						// manda ok
						out.println("Scommessa accettata.");
						System.out.format("Ricevuta scommessa Ip:%s Cavallo:%d Puntata:%d%n", ip.toString(), numCavallo, puntata);
					} else {
						out.println("Scommesse chiuse.");
						System.out.println("Scommessa rifiutata.");
					}
					out.close();
					socket.close();
				} catch (SocketTimeoutException ste) {
					System.out.println("Tempo a disposizione per le scommesse terminato");
				}// catch
				catch (IOException ioe) {
					System.out.println(ioe);
				}
			}// while
		}
	}

	private void comunicaVincitori(InetAddress multiAddress, int multicastPort, int vincente) {
		MulticastSocket socket;
		try {
			socket = new MulticastSocket();
			byte[] buf = new byte[256];
			int quota = 12; // moltiplicatore della puntata vincente
			LinkedList<Scommessa> vincitori = scommesse.get(vincente);

			String message = "";
			if (vincitori.size() == 0)
				message = "Nessun vincitore!";
			else {
				for (Scommessa scommessa : vincitori) {
					message += String.format("Ip: %s Vincita:%d%n", scommessa.getScommettitore(), scommessa.getPuntata() * quota);
				}
			}

			buf = message.getBytes();
			DatagramPacket pk = new DatagramPacket(buf, buf.length, multiAddress, multicastPort);
			socket.send(pk);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		try {
			int sPort = 8001;
			InetAddress multiAddress = InetAddress.getByName("230.0.0.1");
			int multicastPort = 8002;

			Calendar limite = Calendar.getInstance();
			limite.add(Calendar.MINUTE, 1); // la deadline � fissata tra un
											// minuto
			BetServerV2 server = new BetServerV2(sPort, limite);
			System.out.println("Scommesse aperte");

			Calendar now = Calendar.getInstance();
			Thread.sleep(limite.getTimeInMillis() - now.getTimeInMillis());

			int vincente = ((int) (Math.random() * 12)) + 1;
			System.out.println("E' risultato vincente il cavallo: " + vincente);

			server.comunicaVincitori(multiAddress, multicastPort, vincente);
		} catch (InterruptedException ie) {
			System.out.println(ie);
		} catch (UnknownHostException uhe) {
			System.out.println(uhe);
		}
	}

}
