package it.unibo.pcd.akka.cluster.exercises.chat.gui
/** An improved, idiomatic Scala Swing chat application with extracted classes */
import it.unibo.pcd.akka.cluster.exercises.chat.gui.ViewModel.*

import java.awt.Dimension
import java.util.Date
import scala.swing.*

class ChatboxGui(currentUsername: String, otherUsername: String):
  private val user1 = User(currentUsername)
  private val user2 = User(otherUsername)
  private val currentUser = user1
  private val innerChatPanel = new ChatPanel(currentUser)
  var handleNewMessage: (String, String) => Unit = (user, content) => {}
  private def handler(user: String, message: String): Unit =
    handleNewMessage(user, message)
  val inputPanel = new ChatInputPanel(handler, Seq(user1, user2), currentUser)
  val chatPanel: ScrollPane = new ScrollPane(innerChatPanel):
    preferredSize = new Dimension(400, 500)
    horizontalScrollBarPolicy = ScrollPane.BarPolicy.Never

  chatPanel.verticalScrollBar.value = chatPanel.verticalScrollBar.maximum

  def newMessage(user: String, message: String, timestamp: Long): Unit =
    Swing.onEDT:
      innerChatPanel.addMessage(Message(User(user), message, new Date(timestamp)))
      innerChatPanel.refresh()
object ChatboxGui:
  def start(currentUserName: String, otherUsername: String): ChatboxGui =
    val box = ChatboxGui(currentUserName, otherUsername)
    val mainFrame = new MainFrame:
      title = s"Chat - $currentUserName"
      contents = new BorderPanel {
        layout(box.chatPanel) = BorderPanel.Position.Center
        layout(box.inputPanel) = BorderPanel.Position.South
      }
      size = new Dimension(600, 600)
      centerOnScreen()

    mainFrame.visible = true
    box
