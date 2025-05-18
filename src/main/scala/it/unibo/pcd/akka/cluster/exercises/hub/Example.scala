package it.unibo.pcd.akka.cluster.exercises.hub

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem}
import it.unibo.pcd.akka.cluster.exercises.chat.*
import it.unibo.pcd.akka.cluster.exercises.chat.User.UserId
import it.unibo.pcd.akka.cluster.exercises.hub.Hub.HubMessage
import it.unibo.pcd.akka.cluster.exercises.hub.Hub.HubMessage.Login
import it.unibo.pcd.akka.cluster.exercises.hub.gui.{HubPanel, UserStatusManager}

object HubGui:

  /** Creates and returns a new HubPanel instance */
  def start(userId: UserId, session: ActorRef[ChatSessionSpawn.StartChat], hub: ActorRef[HubMessage]): HubPanel =
    new HubPanel(userId, session, hub, new UserStatusManager())

/** Main application entry point */
object HubApplication:
  def main(args: Array[String]): Unit = {
    // Get username from arguments or use default
    val usernameA = "Bibo"
    val usernameB = "Gianluca"
    // Create actor system with hub
    val system = ActorSystem[Unit](
      Behaviors.setup { context =>
        // Create the hub actor
        val hub = context.spawn(Hub(), "hub")
        val session = context.spawn(ChatSessionSpawn(), "chat-session-manager")
        // Create and initialize the hub GUI
        val hubGuiA = HubGui.start(UserId(usernameA), session, hub)
        hubGuiA.initialize(context)
        val hubGuiB = HubGui.start(UserId(usernameB), session, hub)
        hubGuiB.initialize(context)
        hub ! Login(UserId(usernameA))
        hub ! Login(UserId(usernameB))
        Behaviors.empty
      },
      "hub-system"
    )
  }
