package models

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._

case class Task(
    id: Option[String] = None, // Using String for MongoDB ObjectId
    name: String,
    comments: String,
    completed: Boolean)

object Task {
  // Conversion from MongoDB document to Task
  def fromDocument(doc: org.mongodb.scala.Document): Task = {
    Task(
      id = Some(doc.getObjectId("_id").toString),
      name = doc.getString("name"),
      comments = doc.getString("comments"),
      completed = doc.getBoolean("completed")
    )
  }
  
  // Conversion from Task to MongoDB document
  def toDocument(task: Task): org.mongodb.scala.Document = {
    val builder = org.mongodb.scala.Document.builder()
    task.id.foreach(id => builder.append("_id", new ObjectId(id)))
    builder
      .append("name", task.name)
      .append("comments", task.comments)
      .append("completed", task.completed)
      .build()
  }

  /**
    * Mapping to write a Task out as a JSON value.
    */
  implicit val implicitWrites = new Writes[Task] {
    def writes(task: Task): JsValue = {
      Json.obj(
        "id" -> task.id,
        "name" -> task.name,
        "comments" -> task.comments,
        "completed" -> task.completed
      )
    }
  }
  
  implicit val implicitReads: Reads[Task] = Json.reads[Task]
}
