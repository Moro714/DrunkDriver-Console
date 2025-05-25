import java.util.Random;

public class Card {
    private char semn;  // simbolul cărții (ex: D, H, S, C)
    private static final char[] SEMNE = {'D', 'H', 'S', 'C'}; // poți pune Unicode dacă vrei

    public Card() {
        Random r = new Random();
        semn = SEMNE[r.nextInt(SEMNE.length)];
    }

    public char getSemn() {
        return semn;
    }

    @Override
    public String toString() {
        return String.valueOf(semn);
    }
}
