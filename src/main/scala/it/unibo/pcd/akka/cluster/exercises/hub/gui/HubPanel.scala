package it.unibo.pcd.akka.cluster.exercises.hub.gui

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.pcd.akka.cluster.exercises.chat.User.UserId
import it.unibo.pcd.akka.cluster.exercises.hub.ChatSessionSpawn
import it.unibo.pcd.akka.cluster.exercises.hub.Hub.{HubMessage, UserState, UserStatus}

import java.awt.event.{WindowAdapter, WindowEvent}
import scala.swing.{BorderPanel, Button, Dimension, Label, MainFrame, ScrollPane, Swing}
import scala.swing.event.ButtonClicked

/** Main application class for the Hub GUI */
class HubPanel(
    userId: UserId,
    chatSessionSpawn: ActorRef[ChatSessionSpawn.StartChat],
    hub: ActorRef[HubMessage],
    statusManager: UserStatusManager
) extends MainFrame:

  title = s"Hub - ${userId.id}"
  preferredSize = new Dimension(300, 400)

  // UI components
  private val userListView =
    new UserListView(userId, user => chatSessionSpawn ! ChatSessionSpawn.StartChat(userId, user, hub))
  private val statusLabel = new Label(s"Logged in as: ${userId.id}")
  private val logoutButton = new Button("Logout")

  // Set up the UI
  contents = new BorderPanel {
    layout(new ScrollPane(userListView)) = BorderPanel.Position.Center
    layout(new BorderPanel {
      layout(statusLabel) = BorderPanel.Position.West
      layout(logoutButton) = BorderPanel.Position.East
    }) = BorderPanel.Position.South
  }

  // Set up initial state
  pack()
  centerOnScreen()
  open()

  // Register this user with the hub and listen for updates
  def initialize(context: ActorContext[_]): ActorRef[UserStatus] =
    val statusListener = context.spawn(createStatusListener(), s"hub-status-listener-${userId.id}")
    hub ! HubMessage.Register(statusListener)
    hub ! HubMessage.Login(userId)

    // Handle window closing - logout user
    peer.addWindowListener(
      new WindowAdapter:
        override def windowClosing(e: WindowEvent): Unit =
          hub ! HubMessage.Logout(userId)
    )

    // Handle logout button
    listenTo(logoutButton)
    reactions += { case ButtonClicked(`logoutButton`) =>
      hub ! HubMessage.Logout(userId)
      dispose()
    }

    statusListener

  /** Creates an actor behavior to listen for user status updates */
  private def createStatusListener(): Behavior[UserStatus] = Behaviors.receive: (context, message) =>
    message match
      case UserStatus(user, UserState.Online()) =>
        statusManager.markOnline(user)
      case UserStatus(user, UserState.Offline()) =>
        statusManager.markOffline(user)

    // Update the UI on the Swing thread
    Swing.onEDT(updateUserList())
    Behaviors.same

  /** Updates the UI with the current user lists */
  private def updateUserList(): Unit =
    userListView.updateUsers(
      statusManager.getOnlineUsers,
      statusManager.getOfflineUsers
    )
