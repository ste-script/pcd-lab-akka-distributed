package it.unibo.pcd.akka.cluster.exercises.hub

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.akka.cluster.example.CborSerializable
import it.unibo.pcd.akka.cluster.exercises.chat.ChatBox.ChatBoxMessage
import it.unibo.pcd.akka.cluster.exercises.chat.User.UserId
import it.unibo.pcd.akka.cluster.exercises.chat.chat
import it.unibo.pcd.akka.cluster.exercises.hub.Hub.HubMessage

object ChatSessionSpawn:
  case class StartChat(currentUser: UserId, otherUser: UserId, hub: ActorRef[HubMessage]) extends CborSerializable

  def apply(): Behavior[StartChat] =
    Behaviors.receive:
      case (context, StartChat(currentUser, otherUser, hub)) =>
        // Create a new chat session
        val chatBoxHandler = Behaviors.receive[ActorRef[ChatBoxMessage]] { (context, chatBox) =>
          context.spawnAnonymous(chat(currentUser.id, otherUser.id, chatBox))
          Behaviors.same
        }
        hub ! HubMessage.ChatWith(currentUser, otherUser, context.spawnAnonymous(chatBoxHandler))
        Behaviors.same
