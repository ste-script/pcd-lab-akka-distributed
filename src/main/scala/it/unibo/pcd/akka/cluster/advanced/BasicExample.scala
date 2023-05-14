package it.unibo.pcd.akka.cluster.advanced

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.*
import akka.cluster.typed.Cluster
import com.typesafe.config.ConfigFactory
import it.unibo.pcd.akka.cluster.seeds

import concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps
import scala.util.Random
import it.unibo.pcd.akka.cluster.*
import AntsRender.*
// create ants and then an frontend to visualise them.
// It uses role to decide what kind of ActorSystem the node should deploy
object Root:
  def apply(ants: Int = 100, period: FiniteDuration = 60 milliseconds): Behavior[Nothing] = Behaviors.setup { ctx =>
    val cluster = Cluster(ctx.system)
    if (cluster.selfMember.hasRole(Roles.frontend)) AntsRender().narrow
    else
      (0 to ants) foreach (_ => {
        given random: Random = Random()
        val (x, y) = (random.nextInt(width), random.nextInt(height))
        ctx.spawnAnonymous(Ant.apply((x, y), period))
      })
      Behaviors.empty
  //Behaviors.empty
  }

@main def multipleAnts: Unit =
  startupWithRole(Roles.frontend, seeds.head)(Root())
  startupWithRole(Roles.backend, seeds.last)(Root())

@main def anotherFrontend: Unit =
  startupWithRole(Roles.frontend, 8081)(Root())

@main def anotherBackend: Unit =
  startupWithRole(Roles.backend, 8082)(Root())
