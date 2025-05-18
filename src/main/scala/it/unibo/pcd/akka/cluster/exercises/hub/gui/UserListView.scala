package it.unibo.pcd.akka.cluster.exercises.hub.gui

import it.unibo.pcd.akka.cluster.exercises.chat.User.UserId

import java.awt.Color
import scala.swing.{BoxPanel, Button, Dimension, Font, Label, Orientation, Swing}
import scala.swing.event.ButtonClicked

class UserListView(currentUserId: UserId, onChatRequest: UserId => Unit) extends BoxPanel(Orientation.Vertical):
  background = Color.WHITE

  /** Updates the displayed users */
  def updateUsers(onlineUsers: Set[UserId], offlineUsers: Set[UserId]): Unit =
    contents.clear()

    // Add header for online users
    contents += new Label("Online Users:"):
      font = new Font("Dialog", java.awt.Font.BOLD, 14)
      preferredSize = new Dimension(Short.MaxValue, 25)

    // Add online users
    onlineUsers
      .filterNot(_.id == currentUserId.id)
      .foreach: user =>
        val userButton = new Button(user.id):
          preferredSize = new Dimension(Short.MaxValue, 30)
          background = new Color(230, 230, 255)

        listenTo(userButton)
        reactions += { case ButtonClicked(`userButton`) =>
          onChatRequest(user)
        }

        contents += userButton

    // Add spacer
    contents += Swing.VStrut(15)

    // Add header for offline users
    contents += new Label("Offline Users:"):
      font = new Font("Dialog", java.awt.Font.BOLD, 14)
      preferredSize = new Dimension(Short.MaxValue, 25)

    // Add offline users
    offlineUsers.foreach: user =>
      contents += new Label(user.id):
        preferredSize = new Dimension(Short.MaxValue, 25)
        foreground = Color.GRAY

    revalidate()
    repaint()
