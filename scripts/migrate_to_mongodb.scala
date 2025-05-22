import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import java.sql.{Connection, DriverManager, ResultSet}
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._

/**
 * Migration script to transfer data from PostgreSQL to MongoDB
 *
 * Usage: Run this script with the Scala REPL or as a standalone application
 * Make sure both PostgreSQL and MongoDB are running before executing
 */
object MigrateToMongoDB extends App {
  println("Starting migration from PostgreSQL to MongoDB...")
  
  // PostgreSQL connection settings
  val pgUrl = "jdbc:postgresql://localhost:5432/scala_todo"
  val pgUser = "postgres" // Replace with your PostgreSQL username
  val pgPassword = "postgres" // Replace with your PostgreSQL password
  
  // MongoDB connection settings
  val mongoUri = "mongodb://localhost:27017/scala_todo"
  val mongoClient = MongoClient(mongoUri)
  val mongoDatabase = mongoClient.getDatabase("scala_todo")
  val tasksCollection = mongoDatabase.getCollection("tasks")
  
  // Clear existing MongoDB collection
  println("Clearing existing MongoDB collection...")
  Await.result(tasksCollection.drop().toFuture(), 10.seconds)
  
  try {
    // Connect to PostgreSQL
    Class.forName("org.postgresql.Driver")
    val pgConnection = DriverManager.getConnection(pgUrl, pgUser, pgPassword)
    
    // Query all tasks from PostgreSQL
    val statement = pgConnection.createStatement()
    val resultSet = statement.executeQuery("SELECT * FROM task")
    
    // Migrate each task to MongoDB
    var migratedCount = 0
    while (resultSet.next()) {
      val id = resultSet.getLong("id")
      val name = resultSet.getString("name")
      val comments = resultSet.getString("comments")
      val completed = resultSet.getBoolean("completed")
      
      // Create MongoDB document
      val document = Document(
        "legacy_id" -> id,
        "name" -> name,
        "comments" -> comments,
        "completed" -> completed
      )
      
      // Insert into MongoDB
      Await.result(tasksCollection.insertOne(document).toFuture(), 10.seconds)
      migratedCount += 1
    }
    
    println(s"Migration complete! $migratedCount tasks migrated to MongoDB.")
    
    // Close PostgreSQL connection
    resultSet.close()
    statement.close()
    pgConnection.close()
    
  } catch {
    case e: Exception => 
      println(s"Error during migration: ${e.getMessage}")
      e.printStackTrace()
  } finally {
    // Close MongoDB connection
    mongoClient.close()
  }
}