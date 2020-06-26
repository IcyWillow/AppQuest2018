package willow.imemory;

public class MemoryCard {

    public String cardText1 = "";
    public String cardBitmap1;
    public String cardText2 = "";
    public String cardBitmap2;

    public MemoryCard(String txt1, String txt2, String bitmap1, String bitmap2){

        this.cardText1 = txt1;
        this.cardText2 = txt2;

        this.cardBitmap1 = bitmap1;
        this.cardBitmap2 = bitmap2;

    }
}
