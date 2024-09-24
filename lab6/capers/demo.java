import java.io.File;

public class demo {
    public static void main(String[] args) {
        File f = new File("dummy.txt");
        f.createNewFile();
        f.exists();
    }
}
