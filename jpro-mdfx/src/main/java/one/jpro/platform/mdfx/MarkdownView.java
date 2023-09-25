package one.jpro.platform.mdfx;

import one.jpro.platform.mdfx.impl.AdaptiveImage;
import one.jpro.platform.mdfx.impl.MDFXNodeHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MarkdownView extends VBox {

    private final SimpleStringProperty mdString = new SimpleStringProperty("");

    public MarkdownView(String mdString) {
        this.mdString.set(mdString);
        this.mdString.addListener((p,o,n) -> updateContent());
        Optional.ofNullable(MarkdownView.class.getResource("/one/jpro/platform/mdfx/mdfx.css"))
                .ifPresent(cssResource -> getStylesheets().add(cssResource.toExternalForm()));
        getDefaultStylesheets().forEach(s -> getStylesheets().add(s));
        updateContent();
    }

    public MarkdownView() {
        this("");
    }

    protected List<String> getDefaultStylesheets() {
        final var defaultStylesheets = new ArrayList<String>();
        Optional.ofNullable(MarkdownView.class.getResource("/one/jpro/platform/mdfx/mdfx-default.css"))
                .ifPresent(cssResource -> defaultStylesheets.add(cssResource.toExternalForm()));
        return defaultStylesheets;
    }

    private void updateContent() {
        MDFXNodeHelper content = new MDFXNodeHelper(this, mdString.getValue());
        getChildren().clear();
        getChildren().add(content);
    }

    public StringProperty mdStringProperty() {
        return mdString;
    }

    public void setMdString(String mdString) {
        this.mdString.set(mdString);
    }

    public String getMdString() {
        return mdString.get();
    }

    public boolean showChapter(int[] currentChapter) {
            return true;
    }

    public void setLink(Node node, String link, String description) {
        // TODO
        //com.jpro.web.Util.setLink(node, link, scala.Option.apply(description));
    }

    public Node generateImage(String url) {
        if(url.isEmpty()) {
            return new Group();
        } else {
            Image img = new Image(url, false);
            AdaptiveImage r = new AdaptiveImage(img);

            // The TextFlow does not limit the width of its node based on the available width
            // As a workaround, we bind to the width of the MarkDownView.
            r.maxWidthProperty().bind(widthProperty());

            return r;
        }

    }
}
