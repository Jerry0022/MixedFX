package de.mixedfx.assets;

import de.mixedfx.file.DataHandler;
import de.mixedfx.file.FileObject;
import javafx.scene.image.Image;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

import java.io.*;

/**
 * Additionally to the other Handler this Handler applies the prefix before reading / writing. Can write:
 * <ul>
 * <li>Images</li>
 * <li>Strings</li>
 * <li>Files</li>
 * </ul>
 *
 * @author Jerry
 */
@Log4j2(topic = "Assets")
public class MasterHandler {
    /**
     * @param file
     * @param type Supports {@link Image}, {@link String} or other.
     * @return
     */
    public static <T extends Object> T read(final FileObject file, final Class<? extends T> type) {
        if (type.equals(Image.class)) {
            return (T) ImageHandler.readImage(file);
        }
        else if (type.equals(String.class)) {
            try {
                return (T) FileUtils.readFileToString(DataHandler.createOrFindFile(file));
            } catch (IOException | InterruptedException e) {
                log.error("Could not read/access file! " + file);
                return null;
            }

        }
        else {
            try {
                final FileInputStream fileInput = new FileInputStream(DataHandler.createOrFindFile(file));
                if (FileUtils.readFileToString(file.toFile()).isEmpty()) {
                    fileInput.close();
                    return null;
                }
                else {
                    final ObjectInputStream inputStream = new ObjectInputStream(fileInput);
                    final T input = (T) inputStream.readObject();
                    inputStream.close();
                    return input;
                }
            } catch (IOException | InterruptedException | ClassNotFoundException e) {
                log.error("Could not read/access file or class failure! " + file);
                e.printStackTrace();
                return null;
            }
        }
    }

    public static void write(final FileObject file, final Object object) {
        DataHandler.deleteFile(file);
        if (object instanceof Image) {
            try {
                ImageHandler.writeImage(file, (Image) object);
            } catch (final IOException e) {
                log.error("Could not write/access file! " + file);
            }
        }
        else if (object instanceof String) {
            try {
                final PrintStream ps = new PrintStream(new FileOutputStream(DataHandler.createOrFindFile(file)), true);
                ps.write(((String) object).getBytes());
                ps.close();
            } catch (final IOException | InterruptedException e) {
                log.error("Could not write/access file! " + file);
            }
        }
        else {
            try {
                final ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(DataHandler.createOrFindFile(file)));
                outputStream.writeObject(object);
                outputStream.close();
            } catch (final IOException | InterruptedException e) {
                log.error("Could not write/access file! " + file);
            }

        }
    }

    public static void remove(final FileObject file, final Class<?> type) {
        if (type.equals(Image.class)) {
            file.setPrefix(ImageHandler.prefix);
        }
        DataHandler.deleteFile(file);
    }
}
