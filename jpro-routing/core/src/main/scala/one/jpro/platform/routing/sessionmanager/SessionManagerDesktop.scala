package one.jpro.platform.routing.sessionmanager

import one.jpro.platform.routing._
import org.slf4j.{Logger, LoggerFactory}
import simplefx.all._
import simplefx.core._
import simplefx.util.ReflectionUtil

class SessionManagerDesktop(val webApp: RouteNode) extends SessionManager { THIS =>
  assert(webApp != null, "webApp must not be null!")

  private lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  def goBack(): Unit = {
    historyForward = historyCurrent :: historyForward
    historyCurrent = historyBackward.head
    historyBackward = historyBackward.tail
    gotoURL(historyCurrent.path, false)
  }

  def goForward(): Unit = {
    assert(historyForward.nonEmpty, "Can't go forward, there is no entry in the forward history!")
    historyBackward = historyCurrent :: historyBackward
    historyCurrent = historyForward.head
    historyForward = historyForward.tail
    gotoURL(historyCurrent.path, false)
  }

  def gotoURL(_url: String, x: ResponseResult, pushState: Boolean): Response = {
    x match {
      case Redirect(url) =>
        logger.debug(s"redirect: ${_url} -> $url")
        gotoURL(url)
      case view: View =>
        val oldView = this.view
        this.view = view
        view.setSessionManager(this)
        view.url = url

        isFullscreen = view.fullscreen
        container.children = List(view.realContent)
        scrollpane.vvalue = 0.0
        if(oldView != null && oldView != view) {
          oldView.onClose()
          oldView.setSessionManager(null)
          markViewCollectable(oldView, view)
        }
        THIS.view = view

        if(pushState ) {
          historyForward = Nil
          if(historyCurrent != null) {
            historyBackward = historyCurrent :: historyBackward
          }
          historyCurrent = HistoryEntry(url, view.title)
        }
        Response.view(view)
    }
  }
  val container = new StackPane()
  val scrollpane: ScrollPane = if(System.getProperty("routing.scrollpane") != null) {
    ReflectionUtil.callNew(System.getProperty("routing.scrollpane"))().asInstanceOf[ScrollPane]
  } else new ScrollPane()

  customizeScrollpane(scrollpane)
  def customizeScrollpane(scrollpane: ScrollPane): Unit = {
    scrollpane.fitToWidth = true
    scrollpane.content <-- container
    scrollpane.fitToHeight <-- isFullscreen
    scrollpane.style = "-fx-padding: 0;"
    scrollpane.background = Background.EMPTY
    scrollpane.vbarPolicy <-- (if(isFullscreen) ScrollPane.ScrollBarPolicy.NEVER else ScrollPane.ScrollBarPolicy.AS_NEEDED)
  }

  webApp <++ scrollpane
  @Bind var isFullscreen = true
  onceWhen(webApp.scene != null && webApp.scene.window != null) --> {
    val window = webApp.scene.window
    if(window.isInstanceOf[Stage]) {
      window.asInstanceOf[Stage].title <-- (if(view != null) view.title else "")
    }
  }

  def start(): Response = {
    gotoURL("/", pushState = true)
  }
}
