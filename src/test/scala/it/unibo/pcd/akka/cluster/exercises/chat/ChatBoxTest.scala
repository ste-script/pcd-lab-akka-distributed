package it.unibo.pcd.akka.cluster.exercises.chat
import akka.actor.testkit.typed.scaladsl.{BehaviorTestKit, TestInbox}
import it.unibo.pcd.akka.cluster.exercises.chat.ChatBox.History
import org.scalatest.funsuite.AnyFunSuite

class ChatBoxTest extends AnyFunSuite:
  import ChatBox.ChatBoxMessage.*
  import User.*
  test("ChatBox should send and receive messages correctly") {
    // Create a test actor system
    val testKit = BehaviorTestKit(ChatBox())

    // Create a test user message
    val userId = UserId("TestUser")
    val messageText = "Hello, world!"
    val userMessage = UserMessage(userId, messageText, System.currentTimeMillis())

    // Send a new message to the chatbox
    testKit.run(NewMessage(userMessage))

    // Create an inbox for history retrieval
    val historyInbox = TestInbox[History]()

    // Request history
    testKit.run(GetHistory(historyInbox.ref))

    // Verify history contains our message
    val history = historyInbox.receiveMessage()
    assert(history.messages.size == 1)
    assert(history.messages.head == userMessage)

    // Test listening for new messages
    val messageInbox = TestInbox[UserMessage]()
    testKit.run(ListenNewMessages(messageInbox.ref))

    // Send another message
    val secondMessage = UserMessage(userId, "Second message", System.currentTimeMillis())
    testKit.run(NewMessage(secondMessage))

    // Verify the listener received the message
    assert(messageInbox.hasMessages)
    val receivedMessage = messageInbox.receiveMessage()
    assert(receivedMessage == secondMessage)
  }
