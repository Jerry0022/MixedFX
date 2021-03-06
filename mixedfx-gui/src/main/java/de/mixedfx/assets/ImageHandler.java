package de.mixedfx.assets;

import de.mixedfx.file.DataHandler;
import de.mixedfx.file.FileObject;
import de.mixedfx.gui.RegionManipulator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageHandler {
    /**
     * By default this is the {@link ImageProducer#getTransparent()}. If you
     * change this, this is directly applied for all new loaded images!
     */
    public final static ObjectProperty<Image> defaultImage = new SimpleObjectProperty<>(ImageProducer.getTransparent());
    /**
     * If to use JavaFX BackgroundLoading, see also {@link Image}. Default: true
     */
    public static boolean backgroundLoading = true;
    /**
     * The preferred prefix. Default: "img"
     */
    public static String prefix = "img";

    /**
     * The preferred extension (only needed for writing actions). Default: "png"
     */
    public static String extension = "png";

    /**
     * The supported extensions of {@link Image}. Only important for
     * {@link #writeImage(FileObject, Image)}!
     */
    public static String[] supportedExtensions = {"BMP", "GIF", "JPEG", "JPG", "PNG"};

    /**
     * Reads an image (applying the {@link ImageHandler#prefix} is recommended).
     * Doesn't throw an exception because even if the image is not found it
     * returns the {@link #defaultImage} or a transparent one (1x1 pixel).
     *
     * @param fileObject The image to retrieve. The extension can be omitted.
     * @return Returns the found image or a transparent one.
     */
    public static Image readImage(final FileObject fileObject) {
        try {
            return new Image(DataHandler.readFile(fileObject).toURI().toString(), ImageHandler.backgroundLoading);
        } catch (final FileNotFoundException e) {
            return ImageHandler.defaultImage.get() != null ? ImageHandler.defaultImage.get()
                    : ImageProducer.getTransparent();
        }
    }

    /**
     * Writes an image object to the destination. Overwrites existing files.
     *
     * @param destination The destination FileObject. The extension may be overwritten
     *                    with the default one if {@link Image} doesn't support it! See
     *                    also {@link #prefix} and {@link #extension}
     * @param toWrite
     */
    public static void writeImage(final FileObject destination, final Image toWrite) throws IOException {
        if (toWrite != null) {
            boolean supported = false;
            for (String s : supportedExtensions) {
                if (s.equalsIgnoreCase(destination.getExtension())) {
                    supported = true;
                }
            }
            if (!supported) {
                destination.setExtension(ImageHandler.extension);
            }

            final PixelReader pixelReader = toWrite.getPixelReader();
            final int width = (int) toWrite.getWidth();
            final int height = (int) toWrite.getHeight();

            final WritableImage writeableImage = new WritableImage(width, height);
            final PixelWriter pixelWriter = writeableImage.getPixelWriter();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    final Color color = pixelReader.getColor(x, y);
                    pixelWriter.setColor(x, y, color);
                }
            }

            // Delete first an existing file
            DataHandler.deleteFile(destination);
            DataHandler.createFolder(destination);
            ImageIO.write(SwingFXUtils.fromFXImage(writeableImage, null), destination.getExtension(),
                    destination.toFile());
        }
    }

    /**
     * Copies image by reading the source {@link #readImage(FileObject)} and
     * writing it to the destination. See also
     * {@link #writeImage(FileObject, Image)}
     *
     * @param source
     * @param destination
     */
    public static void copyImage(final FileObject source, final FileObject destination) throws IOException {
        ImageHandler.writeImage(destination, ImageHandler.readImage(source));
    }

    /**
     * Removes an image and applies the prefix.
     *
     * @param destination
     */
    public static void deleteImageFormatted(final FileObject destination) {
        destination.setPrefix(ImageHandler.prefix);
        DataHandler.deleteFile(destination);
    }

    /**
     * Removes an image.
     *
     * @param destination
     */
    public static void deleteImage(final FileObject destination) {
        DataHandler.deleteFile(destination);
    }

    /**
     * Returns a {@link HBox} which has as background an image. The image is
     * sized to the full size of the HBox.
     *
     * @param image The image which should be stretched to the full background of
     *              the HBox.
     * @return Returns a HBox with the image put into
     * {@link Region#setBackground(Background)} of the HBox.
     */
    public static HBox getPane(final Image image) {
        final HBox hbox = new HBox();
        RegionManipulator.bindBackground(hbox, image);
        return hbox;
    }
}
