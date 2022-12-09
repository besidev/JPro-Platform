/**
 * Module descriptor.
 *
 * @author Besmir Beqiri
 */
module one.jpro.media {
    requires javafx.controls;
    requires javafx.media;
    requires jpro.webapi;
    requires org.json;

    exports one.jpro.media.recorder;
    exports one.jpro.media.recorder.event;
}