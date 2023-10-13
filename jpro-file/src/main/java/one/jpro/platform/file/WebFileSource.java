package one.jpro.platform.file;

import com.jpro.webapi.WebAPI;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;

import java.io.File;

/**
 * Web file source.
 *
 * @author Besmir Beqiri
 */
public final class WebFileSource extends FileSource<WebAPI.JSFile> {

    public WebFileSource(WebAPI.JSFile jsFile) {
        super(jsFile);
    }

    @Override
    String _getName() {
        return getPlatformFile().getFilename();
    }

    @Override
    long _getSize() {
        return getPlatformFile().getFileSize();
    }

    @Override
    String _getObjectURL() {
        return getPlatformFile().getObjectURL().getName();
    }

    @Override
    public double getProgress() {
        return getPlatformFile().getProgress();
    }

    @Override
    public ReadOnlyDoubleProperty progressProperty() {
        return getPlatformFile().progressProperty();
    }

    @Override
    public File getUploadedFile() {
        return getPlatformFile().getUploadedFile();
    }

    @Override
    public ReadOnlyObjectProperty<File> uploadedFileProperty() {
        return getPlatformFile().uploadedFileProperty();
    }
}
