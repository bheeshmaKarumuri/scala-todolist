package repositories

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.bson.ObjectId
import play.api.Configuration
import models.Task
import scala.concurrent.duration._

@Singleton
class TaskRepository @Inject()(configuration: Configuration)(implicit ec: ExecutionContext) {

  // MongoDB connection setup
  private val mongoUri = configuration.get[String]("mongodb.uri")
  private val client: MongoClient = MongoClient(mongoUri)
  private val database: MongoDatabase = client.getDatabase(mongoUri.split("/").last)
  private val collection: MongoCollection[Document] = database.getCollection("tasks")
  
  // Helper method to convert MongoDB's async results to synchronous for compatibility
  private def await[T](future: Future[T]): T = {
    import scala.concurrent.Await
    Await.result(future, 10.seconds)
  }

  def getById(id: String): Option[Task] = {
    try {
      val objectId = new ObjectId(id)
      val futureResult = collection.find(equal("_id", objectId)).first().toFuture()
      val document = await(futureResult)
      if (document != null) Some(Task.fromDocument(document)) else None
    } catch {
      case _: Exception => None
    }
  }
  
  // For backward compatibility with existing code
  def getById(id: Long): Option[Task] = {
    val futureResult = collection.find().toFuture()
    val documents = await(futureResult)
    documents.find(doc => doc.getInteger("legacy_id", 0) == id)
      .map(Task.fromDocument)
  }

  def all(): List[Task] = {
    val futureResult = collection.find().toFuture()
    val documents = await(futureResult)
    documents.map(Task.fromDocument).toList
  }

  def create(task: Task): Unit = {
    val document = Task.toDocument(task)
    await(collection.insertOne(document).toFuture())
  }

  def update(id: String, task: Task): Unit = {
    try {
      val objectId = new ObjectId(id)
      val updates = combine(
        set("name", task.name),
        set("comments", task.comments),
        set("completed", task.completed)
      )
      await(collection.updateOne(equal("_id", objectId), updates).toFuture())
    } catch {
      case _: Exception => // Handle invalid ID format
    }
  }
  
  // For backward compatibility with existing code
  def update(id: Long, task: Task): Unit = {
    val futureResult = collection.find(equal("legacy_id", id)).first().toFuture()
    val document = await(futureResult)
    if (document != null) {
      val objectId = document.getObjectId("_id")
      val updates = combine(
        set("name", task.name),
        set("comments", task.comments),
        set("completed", task.completed)
      )
      await(collection.updateOne(equal("_id", objectId), updates).toFuture())
    }
  }

  def delete(id: String): Unit = {
    try {
      val objectId = new ObjectId(id)
      await(collection.deleteOne(equal("_id", objectId)).toFuture())
    } catch {
      case _: Exception => // Handle invalid ID format
    }
  }
  
  // For backward compatibility with existing code
  def delete(id: Long): Unit = {
    await(collection.deleteOne(equal("legacy_id", id)).toFuture())
  }
}
