import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String numeJucator;
    private char semn;

    public ClientHandler(Socket socket, String numeJucator) throws IOException {
        this.socket = socket;
        this.numeJucator = numeJucator;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void setSemn(char semn) {
        this.semn = semn;
    }

    public char getSemn() {
        return semn;
    }

    public String getNumeJucator() {
        return numeJucator;
    }

    public void sendMessage(String mesaj) {
        out.println(mesaj);
    }

    public void close() throws IOException {
        socket.close();
    }

    @Override
    public void run() {
        try {
            // Client nu trimite nimic, serverul doar trimite mesaje
            while (!socket.isClosed()) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.out.println(numeJucator + " s-a deconectat.");
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
