package it.unibo.pcd.akka.cluster.exercises.hub

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.pcd.akka.cluster.example.CborSerializable
import it.unibo.pcd.akka.cluster.exercises.chat.ChatBox
import it.unibo.pcd.akka.cluster.exercises.chat.ChatBox.ChatBoxMessage
import it.unibo.pcd.akka.cluster.exercises.chat.User.UserId

/** The `Hub` actor serves as a central coordinator for managing user presence and facilitating chat sessions within a
  * multi-user application. It maintains the state of users (online/offline), allows other actors to listen for user
  * status changes, and manages the creation and retrieval of `ChatBox` actors for direct communication between pairs of
  * users.
  *
  * ==Behavior==
  *
  * The `Hub` processes `HubMessage`s to update its internal state and interact with other actors. Its state consists
  * of:
  *   - A list of `UserStatus` objects, tracking each known user and their current `UserState` (Online/Offline).
  *   - A list of `ActorRef[UserStatus]` representing `listeners` interested in user status updates.
  *   - A map associating a pair of `UserId`s (as a `Set`) with the `ActorRef[ChatBoxMessage]` of the `ChatBox` actor
  *     responsible for their direct chat.
  *
  * Upon receiving a message, the `Hub` typically updates its state and then recursively calls itself with the new
  * state, effectively becoming ready to process the next message.
  *
  * ===Message Handling===
  *
  *   - **`Login(userId: UserId)`**: When a `Login` message is received, the `Hub` updates the status of the specified
  *     `userId` to `UserState.Online()`. If the user was already known, their existing status is replaced; if new, they
  *     are added. It then iterates through all registered `listeners` and sends each one a `UserStatus` message
  *     reflecting the user's new online status. The `Hub` then continues with the updated list of users.
  *   - **`Logout(userId: UserId)`**: Upon receiving a `Logout` message, the `Hub` changes the status of the specified
  *     `userId` to `UserState.Offline()`. Similar to `Login`, this updates or adds the user's status. All registered
  *     `listeners` are then notified of this change by sending them the `UserStatus` message for the user now marked as
  *     offline. The `Hub` continues with the updated list of users.
  *   - **`ChatWith(userIdA: UserId, userIdB: UserId, chatBox: ActorRef[ActorRef[ChatBoxMessage]])`**: This message
  *     requests a `ChatBox` actor for a conversation between `userIdA` and `userIdB`. The `chatBox` parameter is an
  *     `ActorRef` to which the `Hub` will reply with the `ActorRef` of the actual `ChatBox` actor for `userIdA` and
  *     `userIdB`. The `Hub` first checks its internal `chatboxes` map for an existing `ChatBox` actor associated with
  *     the pair `{userIdA, userIdB}`.
  *     - If a `ChatBox` actor already exists for this pair, its `ActorRef` is sent to the `chatBox` (reply-to) actor.
  *     - If no `ChatBox` actor exists, the `Hub` spawns a new anonymous `ChatBox` actor (using
  *       `context.spawnAnonymous(ChatBox())`). This new `ChatBox` actor's `ActorRef` is then stored in the `chatboxes`
  *       map, associated with the user pair, and also sent to the `chatBox` (reply-to) actor. The `Hub` continues with
  *       the potentially updated `chatboxes` map.
  *   - **`Register(listener: ActorRef[UserStatus])`**: When an actor wishes to receive updates about user statuses, it
  *     sends a `Register` message containing its own `ActorRef[UserStatus]`. The `Hub` first adds this `listener` to
  *     its internal list of listeners. Then, to bring the new listener up-to-date, the `Hub` immediately sends it a
  *     `UserStatus` message for every user currently tracked in its `users` list. The `Hub` continues with the updated
  *     list of listeners.
  */
object Hub:
  enum UserState:
    case Online()
    case Offline()

  case class UserStatus(user: UserId, state: UserState) extends CborSerializable

  sealed trait HubMessage extends CborSerializable
  object HubMessage:
    case class Login(userId: UserId) extends HubMessage
    case class Logout(userId: UserId) extends HubMessage
    case class ChatWith(userIdA: UserId, userIdB: UserId, chatBox: ActorRef[ActorRef[ChatBoxMessage]])
        extends HubMessage
    case class Register(listener: ActorRef[UserStatus]) extends HubMessage

  import HubMessage.*
  def apply(
      users: List[UserStatus] = List.empty,
      listeners: List[ActorRef[UserStatus]] = List.empty,
      chatboxes: Map[Set[UserId], ActorRef[ChatBoxMessage]] = Map.empty
  ): Behavior[HubMessage] =
    Behaviors.receive: (context, message) =>
      Behaviors.same
