package it.unibo.pcd.akka.cluster.advanced

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.*
import akka.cluster.typed.Cluster
import com.typesafe.config.ConfigFactory
import it.unibo.pcd.akka.cluster.seeds
import concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random
import it.unibo.pcd.akka.cluster.*
import AntsRender.*
// create ants of generate a frontend to visualise ants. It uses role to decide what kind of ActorSystem the node should deploy
object Root:
  def apply(ants: Int = 100): Behavior[Nothing] = Behaviors.setup { ctx =>
    val cluster = Cluster(ctx.system)
    if (cluster.selfMember.hasRole("frontend")) AntsRender().narrow
    else
      (0 to ants) foreach (_ => {
        given random: Random = Random()
        val (x, y) = (random.nextInt(width), random.nextInt(height))
        ctx.spawnAnonymous(Ant.apply((x, y), 60 milliseconds))
      })
      Behaviors.empty
  //Behaviors.empty
  }

@main def multipleAnts: Unit =
  startupWithRole("frontend", seeds.head)(Root())
  startupWithRole("backend", seeds.last)(Root())

@main def anotherFrontend: Unit =
  startupWithRole("frontend", 8081)(Root())

@main def anotherBackend: Unit =
  startupWithRole("backend", 8082)(Root())
