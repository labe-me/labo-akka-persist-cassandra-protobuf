package me.labe.labo

import akka.actor._

object Service extends App {

  println("Running")

  val config = com.typesafe.config.ConfigFactory.load()
  val system = ActorSystem("Cluster", config)

  val actor = system.actorOf(Props(new EntityActor), "entity-1")
  actor ! Command.Increment(10)
  actor ! Command.Decrement(6)
  actor ! Command.Increment(3)

  println("Press enter for step 2")
  scala.io.StdIn.readLine()

  val actor2 = system.actorOf(Props(new EntityActor), "entity-2")
  actor2 ! Command.Increment(5)
  actor2 ! Command.Decrement(4)

  actor ! "snapshot"
  actor2 ! "snapshot"

  println("Press enter to end")
  scala.io.StdIn.readLine()

  system.terminate()

}

import me.labe.labo.entity._
// import me.labe.labo.entity.Entity._
import akka.persistence._

sealed trait Command
object Command {
  case class Increment(v: Long) extends Command
  case class Decrement(v: Long) extends Command
}

trait EntityEvent

class EntityActor extends PersistentActor with ActorLogging {
  override def persistenceId = self.path.name
  private var lastSnapshotId = 0l
  private var data: Entity = Entity()

  log.debug("{} init", persistenceId)

  val receiveRecover: Receive = {
    case SnapshotOffer(m, s: Entity) =>
      log.debug("{} received snapshot offer:\n{}", persistenceId, s)
      lastSnapshotId = m.sequenceNr
      data = s

    case e: EntityEvent =>
      log.debug("{} recovering event:\n{}", persistenceId, e)
      update(e)

    case e: RecoveryCompleted =>
      log.debug("{} recovery completed, state is:\n{}", persistenceId, data.toString())

    case o =>
      log.warning("{} cannot recover {}", persistenceId, o)
  }

  val receiveCommand: Receive = {
    case Command.Increment(v) =>
      persistAsync(Incremented(System.currentTimeMillis, v)){ e => update(e) }

    case Command.Decrement(v) =>
      persistAsync(Decremented(System.currentTimeMillis, v)){ e => update(e) }

    case "snapshot" =>
      saveSnapshot(data)

    case SaveSnapshotSuccess(metadata) =>
      lastSnapshotId = metadata.sequenceNr

    case "flush" =>
      deleteMessages(lastSnapshotId)
      deleteSnapshots(SnapshotSelectionCriteria(maxSequenceNr=lastSnapshotId - 1))

    case other =>
      log.warning("{} received other command {}", persistenceId, other)
  }

  private def update(e: EntityEvent) = {
    e match {
      case Incremented(_, v) => data = data.copy(counter = data.counter + v)
      case Decremented(_, v) => data = data.copy(counter = data.counter - v)
    }
    log.debug("{} new state is:\n{}", persistenceId, data)
  }

}
