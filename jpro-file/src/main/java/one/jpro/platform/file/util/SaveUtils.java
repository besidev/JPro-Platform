package one.jpro.platform.file.util;

import com.jpro.webapi.WebAPI;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import one.jpro.platform.file.ExtensionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Provides utility methods for saving and downloading files.
 *
 * @author Besmir Beqiri
 */
public interface SaveUtils {

    /**
     * Saves a file using the provided save function.
     * This method should be used only for desktop/native applications.
     *
     * @param fileToSave the file to be saved
     * @param fileType   the file extension to be appended to the file name if it does not already have it
     * @param saveFunction the save function to be used to save the file
     * @return a CompletableFuture representing the asynchronous operation of saving the file
     * @throws NullPointerException if the fileToSave parameter is null
     */
    static CompletableFuture<File> save(File fileToSave, String fileType,
                                        Function<File, CompletableFuture<File>> saveFunction) {
        Objects.requireNonNull(fileToSave, "File to save to cannot be null");

        String filePath = fileToSave.getAbsolutePath();
        if (!filePath.endsWith(fileType)) {
            fileToSave = new File(filePath + fileType);
        }
        return saveFunction.apply(fileToSave);
    }

    /**
     * Saves a file with the given name and type using the provided save function.
     * This method should be used only for desktop/native applications.
     *
     * @param stage       The stage where the save dialog will be shown.
     * @param fileName    The initial file name.
     * @param fileType    The file type (extension) of the file to save.
     * @param saveFunction The function to be called to save the file. This function takes a File parameter
     *                    representing the file to save and returns a {@link CompletableFuture<File>} that will
     *                    complete with the saved file.
     * @return A {@link CompletableFuture<File>} that will complete with the saved file if the user selects a file
     *         to save, or fail with a NullPointerException if the user cancels the save operation.
     */
    static CompletableFuture<File> saveAs(Stage stage, String fileName, String fileType,
                       Function<File, CompletableFuture<File>> saveFunction) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file as...");

        fileChooser.setInitialFileName(fileName);
        ExtensionFilter extensionFilter = ExtensionFilter.of(fileType, fileType);
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(extensionFilter.description(),
                extensionFilter.extensions().stream().map(ext -> "*" + ext).toArray(String[]::new));
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setSelectedExtensionFilter(extFilter);

        // Show save dialog
        File saveToFile = fileChooser.showSaveDialog(stage);
        if (saveToFile != null) {
            return saveFunction.apply(saveToFile);
        } else {
            return CompletableFuture.failedFuture(new NullPointerException("File to save to is null"));
        }
    }

    /**
     * Downloads a file with the given name and type using the provided save function.
     *
     * @param stage        The stage is needed to initialise the WebAPI.
     *                     This is required to show the download dialog in a browser environment.
     * @param fileName     The name of the file to be downloaded.
     * @param fileType     The file type (extension) of the file to be downloaded.
     * @param saveFunction The function to be called to save the file. This function takes a File parameter
     *                     representing the file to save and returns a {@link CompletableFuture<File>} that will
     *                     complete with the saved file.
     * @return A {@link CompletableFuture<File>} that will complete with the downloaded file if the download is successful,
     *         or fail with an exception if the download fails or if the operation is not supported in the current environment.
     * @throws UnsupportedOperationException If the download operation is not supported in the current environment.
     */
    static CompletableFuture<File> download(Stage stage, String fileName, String fileType,
                         Function<File, CompletableFuture<File>> saveFunction) {
        if (WebAPI.isBrowser()) {
            WebAPI webAPI = WebAPI.getWebAPI(stage);
            final Logger logger = LoggerFactory.getLogger(SaveUtils.class);
            final File tempFile = createTempFile(fileName, fileType);
            return saveFunction.apply(tempFile).thenCompose(file -> {
                try {
                    final URL fileUrl = file.toURI().toURL();
                    Platform.runLater(() -> webAPI.downloadURL(fileUrl, file::delete));
                    return CompletableFuture.completedFuture(file);
                } catch (IOException ex) {
                    return CompletableFuture.failedFuture(ex);
                }
            }).exceptionallyCompose(ex -> {
                if (!tempFile.delete()) {
                    logger.warn("Could not delete temporary file {}", tempFile.getAbsolutePath());
                }
                logger.error("Error while downloading file", ex);
                return CompletableFuture.failedFuture(ex);
            });
        } else {
            throw new UnsupportedOperationException("Download is only supported in the browser");
        }
    }

    /**
     * Creates a temporary file with the given file name and file type.
     *
     * @param fileName The name of the file.
     * @param fileType The file type (extension) of the file.
     * @return A {@link File} object representing the created temporary file.
     */
    static File createTempFile(String fileName, String fileType) {
        // Get user home directory
        final File tempDir = new File(System.getProperty("user.home") + "/.jpro/tmp");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        String fullFileName = fileName;
        if (!fullFileName.endsWith(fileType)) {
            fullFileName = fullFileName.substring(0, fullFileName.lastIndexOf('.')) + fileType;
        }
        return new File(tempDir, fullFileName);
    }
}
