package it.unibo.pcd.akka.cluster.exercises.hub.gui

import it.unibo.pcd.akka.cluster.exercises.chat.User.UserId

import scala.collection.mutable

/** Manages the user status (online/offline) */
class UserStatusManager:
  private val onlineUsers = mutable.Set[UserId]()
  private val offlineUsers = mutable.Set[UserId]()

  def markOnline(user: UserId): Unit =
    onlineUsers += user
    offlineUsers -= user

  def markOffline(user: UserId): Unit =
    onlineUsers -= user
    offlineUsers += user

  def getOnlineUsers: Set[UserId] = onlineUsers.toSet

  def getOfflineUsers: Set[UserId] = offlineUsers.toSet
