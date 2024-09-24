package capers;
import java.io.File;
import java.io.IOException;

public class demo {
    public static void main(String[] args) throws IOException {
        File d = new File("dummy");
        d.mkdir();
    }
}
