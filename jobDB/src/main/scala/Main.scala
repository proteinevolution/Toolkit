import actors.JobDatabaseActor
import akka.actor.{ActorSystem, PoisonPill}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import akkaguice.{AkkaModule, GuiceAkkaExtension}
import com.google.inject.Guice
import config.ConfigModule

import scala.concurrent.Await

// return (Actor) applicationContext.getBean(actorBeanName);

import scala.concurrent.duration.Duration


object Main extends App  {

  val injector = Guice.createInjector(new ConfigModule(), new AkkaModule(), new modules.DatabaseModule())
  val system = injector.getInstance(classOf[ActorSystem])


  system.actorOf(
    ClusterSingletonManager.props(
      GuiceAkkaExtension(system).props(JobDatabaseActor.name),
      PoisonPill,
      ClusterSingletonManagerSettings(system).withRole("jobDB")
    ), "jobDB")

  Await.result(system.whenTerminated, Duration.Inf)
}



/*


/**
 * A main class to start up the application.
 */
object Main extends App {




  // this could be called inside a supervisor actor to create a supervisor hierarchy,
  // using context.actorOf(GuiceAkkaExtension(context.system)...
  val counter = system.actorOf(GuiceAkkaExtension(system).props(CountingActor.name))

  // tell it to count three times
  counter ! Count
  counter ! Count
  counter ! Count

  // Create a second counter to demonstrate that `AuditCompanion` is injected under Prototype
  // scope, which means that every `CountingActor` will get its own instance of `AuditCompanion`.
  // However `AuditBus` is injected under Singleton scope. Therefore every `AuditCompanion`
  // will get a reference to the same `AuditBus`.
  val counter2 = system.actorOf(GuiceAkkaExtension(system).props(CountingActor.name))
  counter2 ! Count
  counter2 ! Count

  // print the result
  for {
    actor <- Seq(counter, counter2)
    result <- actor.ask(Get)(3.seconds).mapTo[Int]
  } {
    println(s"Got back $result from $counter")
  }

  system.shutdown()
  system.awaitTermination()
}



 */