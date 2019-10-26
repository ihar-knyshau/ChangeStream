package persistance;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileChangeStreamResumer implements ChangeStreamResumer {


    public void storeResumeInfo(String data, String filePath) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            byte[] strToBytes = data.getBytes();
            outputStream.write(strToBytes);
        }
    }

    public String queryResumeInfo(String filePath) throws IOException {
        StringBuilder data = new StringBuilder();
        try (FileInputStream fin = new FileInputStream(filePath)) {
            int i;
            while ((i = fin.read()) != -1) {
                data.append((char) i);
            }
        }
        return data.toString();
    }
}
