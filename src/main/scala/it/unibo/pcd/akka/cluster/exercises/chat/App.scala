package it.unibo.pcd.akka.cluster.exercises.chat

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.typed.ClusterSingleton
import com.typesafe.config.ConfigFactory
import it.unibo.pcd.akka.cluster.exercises.chat.ChatBox.{ChatBoxMessage, History}
import it.unibo.pcd.akka.cluster.exercises.chat.ChatBox.ChatBoxMessage.{GetHistory, ListenNewMessages, NewMessage}
import it.unibo.pcd.akka.cluster.exercises.chat.User.{UserId, UserMessage}
import it.unibo.pcd.akka.cluster.exercises.chat.gui.ChatboxGui
import akka.cluster.typed.SingletonActor
import it.unibo.pcd.akka.cluster.example.CborSerializable

object User:
  import ChatBox.ChatBoxMessage
  case class UserId(id: String)
  case class UserMessage(userId: UserId, message: String, timestamp: Long) extends CborSerializable
  def apply(userId: UserId, chatbox: ActorRef[ChatBoxMessage]): Behavior[String] =
    Behaviors.setup { context =>
      context.log.info(s"User ${userId.id} created")
      Behaviors.receiveMessage { message =>
        chatbox ! ChatBoxMessage.NewMessage(UserMessage(userId, message, System.currentTimeMillis()))
        Behaviors.same
      }
    }

def chat(user: String, other: String, chatBox: ActorRef[ChatBoxMessage]): Behavior[Unit] =
  Behaviors.setup: context =>
    val panel = ChatboxGui.start(user, other)
    val activeUser = context.spawn(User(UserId(user), chatBox), user)
    panel.handleNewMessage = (user, message) => activeUser ! message
    // get history with ask pattern
    val historyInitializer = Behaviors.receiveMessage[History]: messages =>
      messages.messages.foreach(message => panel.newMessage(message.userId.id, message.message, message.timestamp))
      Behaviors.stopped
    val listener: Behavior[UserMessage] = Behaviors.receiveMessage: message =>
      panel.newMessage(message.userId.id, message.message, message.timestamp)
      Behaviors.same
    chatBox ! GetHistory(context.spawnAnonymous(historyInitializer))
    chatBox ! ListenNewMessages(context.spawnAnonymous(listener))
    Behaviors.empty

def rootBehavior: Behavior[Unit] =
  Behaviors.setup { context =>
    val chatbox = context.spawn(ChatBox(), "chatbox")
    val chatOne = context.spawnAnonymous(chat("Gianluca", "Bibo", chatbox))
    val chatTwo = context.spawnAnonymous(chat("Bibo", "Gianluca", chatbox))
    Behaviors.empty
  }

@main
def runChatApp(): Unit =
  val system = ActorSystem(rootBehavior, "chat-system")

@main
def startChatboxNode(): Unit =
  // Override the configuration of the port
  val config = ConfigFactory
    .parseString(s"""akka.remote.artery.canonical.port=25251""")
    .withFallback(ConfigFactory.load("chatbox_cluster"))

  val chatbox = Behaviors.setup: context =>
    val manager = ClusterSingleton(context.system)
    manager.init(SingletonActor(ChatBox(), "chatbox"))
    Behaviors.same
  // Create an Akka system
  ActorSystem(chatbox, "chatbox", config)

def startUserNode(name: String, otherUser: String, port: Int): Unit =
  // Override the configuration of the port
  val config = ConfigFactory
    .parseString(s"""akka.remote.artery.canonical.port=$port""")
    .withFallback(ConfigFactory.load("chatbox_cluster"))

  val userBehavior = Behaviors.setup: context =>
    val manager = ClusterSingleton(context.system)
    context.spawnAnonymous(chat(name, otherUser, manager.init(SingletonActor(ChatBox(), "chatbox"))))
    Behaviors.same
  // Create an Akka system
  ActorSystem(userBehavior, "chatbox", config)

@main
def startGianluca(): Unit =
  startUserNode("Gianluca", "Bibo", 25253)

@main
def startBibo(): Unit =
  startUserNode("Bibo", "Gianluca", 25252)
