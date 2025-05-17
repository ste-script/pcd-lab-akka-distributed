package it.unibo.pcd.akka.cluster.exercises.chat.gui

import java.util.Date

object ViewModel:
  case class User(name: String)

  case class Message(
      sender: User,
      content: String,
      timestamp: Date = new Date()
  )
