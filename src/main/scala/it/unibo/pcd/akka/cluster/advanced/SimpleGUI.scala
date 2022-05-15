package it.unibo.pcd.akka.cluster.advanced

import javax.swing.{JFrame, JPanel, SwingUtilities}
import java.awt.{Canvas, Color, Dimension, Graphics}

class SimpleGUI(val width: Int, val height: Int):
  self => // self-types, used to take the val reference inside the inner class
  private val elementWidth = 10
  private val frame = JFrame()
  private val canvas = Environment()
  frame.setSize(width, height)
  frame.setVisible(true)
  canvas.setVisible(true)
  frame.setLocationRelativeTo(null)
  canvas.setSize(width, height)
  frame.add(canvas)
  def render(elements: List[(Int, Int)]): Unit = SwingUtilities.invokeLater { () =>
    canvas.elements = elements
    canvas.invalidate()
    canvas.repaint()
  }

  private class Environment() extends JPanel:
    var elements: List[(Int, Int)] = List.empty
    override def getPreferredSize = new Dimension(self.width, self.height)
    override def paintComponent(graphics: Graphics): Unit =
      graphics.clearRect(0, 0, self.width, self.height)
      graphics.setColor(Color.BLACK)
      elements.foreach((x, y) => graphics.drawOval(x, y, elementWidth, elementWidth))
