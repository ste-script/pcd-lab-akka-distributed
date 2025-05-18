package it.unibo.pcd.akka.cluster.exercises.chat

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.akka.cluster.example.CborSerializable

/** The `ChatBox` actor is responsible for managing the messages within a specific, self-contained chat session,
  * typically between two users. It maintains a history of messages exchanged in that session and allows other actors to
  * listen for new messages as they arrive.
  *
  * ==Behavior==
  *
  * A `ChatBox` actor manages its state through two primary components:
  *   - `history`: A `List[UserMessage]` that stores all messages sent within this chat session in chronological order.
  *   - `listeners`: A `List[ActorRef[UserMessage]]` containing actors that have subscribed to receive new messages
  *     posted to this `ChatBox` in real-time.
  *
  * The actor processes `ChatBoxMessage`s to interact with its state and other actors. State changes are handled by
  * recursively calling its `apply` method with the updated state, ensuring immutability.
  *
  * ===Message Handling===
  *
  *   - **`NewMessage(userMessage: UserMessage)`**: When a `NewMessage` is received, it means that a new message being
  *     posted to this chat. The `ChatBox` actor performs the following actions:
  *     1. Appends the `userMessage` to its internal `history` list.
  *     2. Notifies all currently registered `listeners` by sending each of them the `userMessage` that was just
  *        received.
  *     3. Continues its behavior with the updated `history` (now including the new message) and the same list of
  *        `listeners`.
  *   - **`GetHistory(replyTo: ActorRef[List[UserMessage]])`**: This message is a request from another actor (specified
  *     by `replyTo`) to retrieve the entire conversation history of this `ChatBox`.
  *     1. Sends the current `history` (a `List[UserMessage]`) as a message directly to the `replyTo` actor.
  *     2. The internal state of the `ChatBox` (history and listeners) remains unchanged. It continues with
  *        `Behaviors.same`, indicating no change in its behavior or state.
  *   - **`ListenNewMessages(replyTo: ActorRef[UserMessage])`**: An actor sends this message to subscribe to new
  *     messages posted in this `ChatBox`. The `replyTo` parameter is the `ActorRef` of the actor wishing to listen.
  *     1. Adds the `replyTo` actor to its internal `listeners` list.
  *     2. Continues its behavior with the same `history` but with the `listeners` list now including the new
  *        subscriber. Any subsequent `NewMessage` will also be sent to this new listener.
  *
  * The `apply` method, taking `history` and `listeners` as parameters, defines the actor's behavior for processing
  * these messages and managing its state for the chat session.
  */
object ChatBox:
  import User.*

  case class History(messages: List[UserMessage]) extends CborSerializable
  sealed trait ChatBoxMessage extends CborSerializable
  object ChatBoxMessage:
    case class NewMessage(message: UserMessage) extends ChatBoxMessage
    case class GetHistory(to: ActorRef[History])
        extends ChatBoxMessage // any actor whom is able to receive a List[UserMessage]
    case class ListenNewMessages(replyTo: ActorRef[UserMessage]) extends ChatBoxMessage

  import ChatBoxMessage.*

  def apply(
      history: List[UserMessage] = List.empty,
      listeners: List[ActorRef[UserMessage]] = List.empty
  ): Behavior[ChatBoxMessage] =
    Behaviors.receive: (context, message) =>
      Behaviors.same // TODO
