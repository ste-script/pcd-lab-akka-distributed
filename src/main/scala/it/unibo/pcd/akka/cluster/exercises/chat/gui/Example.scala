package it.unibo.pcd.akka.cluster.exercises.chat.gui

object Example extends App:
  val box = ChatboxGui.start("Gianluca", "Bibo")
  box.handleNewMessage = (who: String, message: String) => box.newMessage(who, message, System.currentTimeMillis())
