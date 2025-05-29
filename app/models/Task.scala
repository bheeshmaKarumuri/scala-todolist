package models

import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.collection.immutable.Document
import play.api.libs.json._
import scala.jdk.CollectionConverters._

case class Task(
    id: Option[String] = None,
    name: String,
    comments: String,
    completed: Boolean,
    labels: List[String] = List.empty)

object Task {
  // Conversion from MongoDB document to Task
  def fromDocument(doc: Document): Task = {
    Task(
      id = Some(doc.getObjectId("_id").toHexString),
      name = doc.getString("name"),
      comments = doc.getString("comments"),
      completed = doc.getBoolean("completed"),
      labels = Option(doc.getList("labels", classOf[String])).map(_.asScala.toList).getOrElse(List.empty)
    )
  }
  
  // Conversion from Task to MongoDB document
  def toDocument(task: Task): Document = {
    var builder = Document()
    task.id.foreach(id => builder = builder.+(("_id", new ObjectId(id))))
    builder = builder
      .+("name" -> task.name)
      .+("comments" -> task.comments)
      .+("completed" -> task.completed)
      .+("labels" -> task.labels)
    builder
  }

  /**
    * Mapping to write a Task out as a JSON value.
    */
  implicit val implicitWrites: Writes[Task] = new Writes[Task] {
    def writes(task: Task): JsValue = {
      Json.obj(
        "id" -> task.id,
        "name" -> task.name,
        "comments" -> task.comments,
        "completed" -> task.completed,
        "labels" -> task.labels
      )
    }
  }
  
  implicit val implicitReads: Reads[Task] = Json.reads[Task]
}
