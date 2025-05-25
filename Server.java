import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    private static final char[] SEMNE = {'A', 'B', 'C', 'D'};
    private static final int DRUM_LUNGIME_DEFAULT = 7;

    private ServerSocket serverSocket;
    private List<ClientHandler> jucatori = new ArrayList<>();
    private Map<Character, ClientHandler> semnToClient = new HashMap<>();
    private List<Carte> pachet = new ArrayList<>();

    private int numarCartiIntoarse;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server pornit pe port " + port);

        Scanner scanner = new Scanner(System.in);

        System.out.print("Introdu numarul de jucatori (max 4): ");
        int numarJucatori = scanner.nextInt();
        if (numarJucatori < 2 || numarJucatori > 4) {
            System.out.println("Numar jucatori invalid, setez 2.");
            numarJucatori = 2;
        }

        System.out.print("Introdu numarul de carti care se vor intoarce: ");
        numarCartiIntoarse = scanner.nextInt();
        if (numarCartiIntoarse <= 0) {
            System.out.println("Numar carti invalid, setez " + DRUM_LUNGIME_DEFAULT);
            numarCartiIntoarse = DRUM_LUNGIME_DEFAULT;
        }

        // Accepta jucatori
        System.out.println("Astept " + numarJucatori + " jucatori...");
        for (int i = 0; i < numarJucatori; i++) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler ch = new ClientHandler(clientSocket, SEMNE[i], "Jucator " + (i+1));
            jucatori.add(ch);
            semnToClient.put(SEMNE[i], ch);
            new Thread(ch).start();
            System.out.println("Jucator " + (i+1) + " conectat cu semnul " + SEMNE[i]);
        }

        // Pregatim pachetul cu carti: repetam semnele in mod aleator, dupa numarul de carti
        Random rand = new Random();
        for (int i = 0; i < numarCartiIntoarse; i++) {
            char semnAleator = SEMNE[rand.nextInt(numarJucatori)];
            pachet.add(new Carte(semnAleator));
        }

        // Trimitem jucatorilor semnele si nr de carti
        for (ClientHandler ch : jucatori) {
            ch.sendMessage("NUMAR_CARTI:" + numarCartiIntoarse);
            ch.sendMessage("Ai semnul: " + ch.getSemn());
        }

        // Pornim jocul
        joc();
    }

    private void joc() {
        Map<ClientHandler, Integer> pozitii = new HashMap<>();
        for (ClientHandler ch : jucatori) {
            pozitii.put(ch, 0);
        }

        for (int i = 0; i < numarCartiIntoarse; i++) {
            Carte carte = pachet.get(i);
            broadcast("Carte intoarsa #" + i + ": " + carte.getSemn());

            boolean cinevaAvanseaza = false;
            for (ClientHandler ch : jucatori) {
                if (ch.getSemn() == carte.getSemn()) {
                    int pozCurenta = pozitii.get(ch);
                    if (pozCurenta < numarCartiIntoarse) {
                        pozitii.put(ch, pozCurenta + 1);
                        ch.sendMessage("Ai avansat la pozitia " + (pozCurenta + 1) + "/" + numarCartiIntoarse);
                        cinevaAvanseaza = true;
                    }
                }
            }
            if (!cinevaAvanseaza) {
                broadcast("Nimeni nu avanseaza.");
            }
            // Delay scurt intre carti (optional)
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        }

        // Determinam castigatorul
        int maxPoz = 0;
        List<ClientHandler> castigatori = new ArrayList<>();
        for (ClientHandler ch : jucatori) {
            int poz = pozitii.get(ch);
            if (poz > maxPoz) {
                maxPoz = poz;
                castigatori.clear();
                castigatori.add(ch);
            } else if (poz == maxPoz) {
                castigatori.add(ch);
            }
        }

        if (castigatori.size() == 1) {
            broadcast("Castigator: " + castigatori.get(0).getNumeJucator() + " cu pozitia " + maxPoz);
        } else {
            StringBuilder sb = new StringBuilder("Egalitate intre: ");
            for (ClientHandler ch : castigatori) {
                sb.append(ch.getNumeJucator()).append(", ");
            }
            broadcast(sb.substring(0, sb.length() - 2));
        }

        // Afisam scoreboard
        broadcast("=== SCOREBOARD ===");
        for (ClientHandler ch : jucatori) {
            broadcast(ch.getNumeJucator() + " : " + pozitii.get(ch));
        }

        // Inchidem conexiunile
        for (ClientHandler ch : jucatori) {
            ch.close();
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcast(String mesaj) {
        for (ClientHandler ch : jucatori) {
            ch.sendMessage(mesaj);
        }
    }

    public static void main(String[] args) {
        try {
            new Server(12345);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Clasa interna pentru ClientHandler
    class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private char semn;
        private String numeJucator;
        private boolean running = true;

        public ClientHandler(Socket socket, char semn, String nume) throws IOException {
            this.socket = socket;
            this.semn = semn;
            this.numeJucator = nume;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        }

        public char getSemn() {
            return semn;
        }

        public String getNumeJucator() {
            return numeJucator;
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }

        public void close() {
            running = false;
            try {
                socket.close();
            } catch (IOException ignored) {}
        }

        @Override
        public void run() {
            try {
                while (running) {
                    String input = in.readLine();
                    if (input == null) break;
                    // Pentru acest joc simplu, nu avem input de la client in timpul jocului
                }
            } catch (IOException e) {
                System.out.println("Conexiune inchisa cu " + numeJucator);
            }
        }
    }

    // Clasa Carte simpla
    class Carte {
        private char semn;

        public Carte(char semn) {
            this.semn = semn;
        }

        public char getSemn() {
            return semn;
        }
    }
}
