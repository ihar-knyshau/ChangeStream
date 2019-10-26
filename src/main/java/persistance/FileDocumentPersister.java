package persistance;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class FileDocumentPersister implements DocumentPersister {
    private String persistFile;

    public FileDocumentPersister(Properties properties) {
        this.persistFile = properties.getProperty("documents_file_path");
    }

    @Override
    public void persistDocument(String content) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(persistFile, true)) {
            byte[] strToBytes = content.getBytes();
            outputStream.write(strToBytes);
            outputStream.write(System.lineSeparator().getBytes());
            outputStream.write(System.lineSeparator().getBytes());
        }
    }
}
