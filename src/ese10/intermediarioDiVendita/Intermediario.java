package ese10.intermediarioDiVendita;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Intermediario {
    public static final int TCP_PORT = 2345;
    public static final int UDP_PORT = 6789;
    public static final String IP = "127.0.0.1";
    private final List<Venditore> venditori;

    public Intermediario(List<Venditore> venditori) {
        this.venditori = venditori;
        new RequestAcceptor().start();
    }

    private class RequestAcceptor extends Thread {
        ServerSocket serverSocket;
        public void run() {
            try {
                serverSocket = new ServerSocket(TCP_PORT);
                while (true) {
                    Socket cliente = serverSocket.accept();
                    new RequestHandler(cliente).start();
                }
            } catch (IOException e) {
                try {
                    serverSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }

        }
    }

    private class RequestHandler extends Thread {
        private final Socket cliente;
        public RequestHandler(Socket cliente) {
            this.cliente = cliente;
        }
        public void run() {
            try {
                PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                String richiesta = in.readLine();
                if (!checkRichiesta(richiesta)) {
                    out.println("ERROR - Request not acceptable: " + richiesta);
                    return;
                }
                out.println("OK - Request accepted: " + richiesta);
                for (Venditore v : venditori) {
                    DatagramSocket datagramSocket = new DatagramSocket();
                    InetAddress vendAddress = InetAddress.getByName(v.getAddress());
                    byte[] buf = richiesta.getBytes();
                    DatagramPacket dp = new DatagramPacket(buf, buf.length, vendAddress, v.getPort());
                    datagramSocket.send(dp);
                    new VendorResponseHandler(datagramSocket).start();
                }
                TimeUnit.MINUTES.sleep(1);


            } catch (IOException | InterruptedException e) { e.printStackTrace(); }
        }
    }

    private class VendorResponseHandler extends Thread {
        private final DatagramSocket datagramSocket;
        public VendorResponseHandler(DatagramSocket datagramSocket) {
            this.datagramSocket = datagramSocket;
        }
        public void run() {
            try {
                datagramSocket.setSoTimeout(1000 * 60);
                byte[] buf = new byte[1024];
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                datagramSocket.receive(dp);
                String risposta = new String(dp.getData());
                if(!checkRisposta(risposta)) {

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkRichiesta(String req) {
        String[] split = req.split(",");
        if (split.length != 2)
            return false;
        try {
            //idProdotto,quantità
            int idProd = Integer.parseInt(split[0]);
            int quant = Integer.parseInt(split[1]);
            return idProd > 0 && quant > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean checkRisposta(String res) {
        String[] split = res.split(",");
        if (split.length != 4)
            return false;
        try {
            //idProdotto,quantità,prezzoTotale,idVenditore
            int idProd = Integer.parseInt(split[0]);
            int quant = Integer.parseInt(split[1]);
            int prezzoTot = Integer.parseInt(split[2]);
            int idVend = Integer.parseInt(split[3]);
            return idProd > 0 && quant > 0 && prezzoTot>0 && idVend>0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
