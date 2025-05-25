import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            int numarCartiIntoarse = 0;
            char semn = '?';

            while (true) {
                String mesaj = in.readLine();
                if (mesaj == null) break;

                if (mesaj.startsWith("NUMAR_CARTI:")) {
                    numarCartiIntoarse = Integer.parseInt(mesaj.substring("NUMAR_CARTI:".length()));
                    System.out.println("JOCUL A INCEPUT! Drum lungime: " + numarCartiIntoarse);
                } else if (mesaj.startsWith("Ai semnul:")) {
                    semn = mesaj.charAt(mesaj.length() - 1);
                    System.out.println("Ai semnul: " + semn);
                } else {
                    System.out.println(mesaj);
                }
            }

            socket.close();

        } catch (IOException e) {
            System.out.println("Nu s-a putut conecta la server.");
            e.printStackTrace();
        }
    }
}
