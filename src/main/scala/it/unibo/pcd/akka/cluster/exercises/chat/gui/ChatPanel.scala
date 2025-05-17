package it.unibo.pcd.akka.cluster.exercises.chat.gui
import ViewModel.*

import java.awt.Color
import java.text.SimpleDateFormat
import scala.swing.{BoxPanel, FlowPanel, Orientation, Swing}

class ChatPanel(currentUser: User) extends BoxPanel(Orientation.Vertical):
  background = Color.WHITE

  private val messages = scala.collection.mutable.ArrayBuffer[Message]()
  private val timeFormatter = new SimpleDateFormat("HH:mm")

  def addMessage(msg: Message): Unit =
    messages += msg
    refresh()

  def getMessages: Seq[Message] = messages.toSeq

  def refresh(): Unit =
    contents.clear()
    contents += Swing.VGlue

    // Add messages with reduced spacing between bubbles
    contents ++= messages.map: msg =>
      val bubble = new MessageBubble(msg, currentUser)
      val isCurrentUser = msg.sender.name == currentUser.name

      new FlowPanel(FlowPanel.Alignment.Left)():
        background = Color.WHITE
        hGap = 2 // Reduced horizontal gap

        // Current user messages on right, other user messages on left
        contents ++= Seq(Swing.HGlue, bubble)

    contents += Swing.VGlue
    revalidate()
    repaint()

