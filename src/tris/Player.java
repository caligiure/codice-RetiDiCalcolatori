package tris;

import java.net.Socket;

public class Player {
    private final String username;
    private final Socket sock;

    public Player(Socket sock, String username) {
        this.sock = sock;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public Socket getSock() {
        return sock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return player.getUsername().equals(getUsername());
    }

    @Override
    public int hashCode() {
        return sock != null ? sock.hashCode() : 0;
    }

    @Override
    public String toString() {
        return username;
    }
}