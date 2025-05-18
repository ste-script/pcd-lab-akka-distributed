package it.unibo.pcd.akka.cluster.exercises.chat.gui

import it.unibo.pcd.akka.cluster.exercises.chat.gui.ViewModel.{Message, User}

import java.awt.{Color, Font}
import java.text.SimpleDateFormat
import javax.swing.border.EmptyBorder
import scala.swing.{Alignment, BoxPanel, Label, Orientation}

// UI Components as separate classes
class MessageBubble(msg: Message, currentUser: User) extends BoxPanel(Orientation.Vertical):
  private val isCurrentUser = msg.sender.name == currentUser.name
  private val colors = Map(
    "current" -> new Color(219, 234, 254), // Light blue for current user
    "other" -> new Color(220, 252, 231), // Light green for other user
    "time" -> new Color(107, 114, 128) // Gray for timestamp
  )

  background = if (isCurrentUser) colors("current") else colors("other")
  border = new EmptyBorder(6, 10, 6, 10) // Reduced padding for less vertical space

  private val timeFormatter = new SimpleDateFormat("HH:mm")

  // Message content with reduced vertical spacing
  contents += new Label(msg.content):
    font = new Font("Sans-Serif", Font.PLAIN, 14)
    xAlignment = Alignment.Left

  // Sender name
  contents += new Label(if (isCurrentUser) "yourself" else msg.sender.name):
    foreground = colors("time")
    font = new Font("Sans-Serif", Font.ITALIC, 11) // Small italic font
    xAlignment = if (isCurrentUser) Alignment.Right else Alignment.Left

  // Timestamp with reduced font size
  contents += new Label(timeFormatter.format(msg.timestamp)):
    foreground = colors("time")
    font = new Font("Sans-Serif", Font.ITALIC, 9) // Smaller font
    xAlignment = if (isCurrentUser) Alignment.Right else Alignment.Left
