package it.unibo.pcd.akka.cluster.exercises.chat.gui

import java.awt.{Color, Font}
import scala.swing.{BorderPanel, Label, TextField}
import scala.swing.event.{Key, KeyPressed}
import ViewModel.*
class ChatInputPanel(onSendMessage: (String, String) => Unit, users: Seq[User], currentUser: User) extends BorderPanel:
  private val messageField = new TextField { columns = 30 }
  private val currentUserLabel = new Label(s"Sending as: ${currentUser.name}"):
    foreground = new Color(59, 130, 246) // Blue to highlight current user
    font = new Font("Sans-Serif", Font.BOLD, 12)

  // Use key event for sending
  listenTo(messageField.keys)
  reactions += { case KeyPressed(_, Key.Enter, _, _) =>
    sendMessage(currentUser)
  }

  private def sendMessage(sender: User): Unit =
    val content = messageField.text.trim
    if (content.nonEmpty) {
      onSendMessage(sender.name, content)
      messageField.text = ""
    }

  // Layout components
  layout(currentUserLabel) = BorderPanel.Position.West
  layout(messageField) = BorderPanel.Position.Center
