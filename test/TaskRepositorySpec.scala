
import org.scalatestplus.play._
import play.api.Configuration
import scala.concurrent.ExecutionContext.Implicits.global
import repositories.TaskRepository
import org.mongodb.scala._
import org.mongodb.scala.bson.ObjectId

class TaskRepositorySpec extends PlaySpec {

  // Create test configuration with MongoDB test database
  val testConfig = Configuration.from(Map(
    "mongodb.uri" -> "mongodb://localhost:27017/scala_todo_test"
  ))
  
  // Create repository with test configuration
  def taskService: TaskRepository = new TaskRepository(testConfig)
  
  // Clear the test collection before each test
  private def clearCollection(): Unit = {
    val client = MongoClient("mongodb://localhost:27017")
    val database = client.getDatabase("scala_todo_test")
    val collection = database.getCollection("tasks")
    import scala.concurrent.Await
    import scala.concurrent.duration._
    Await.result(collection.drop().toFuture(), 10.seconds)
  }
  
  // Clear the collection before running tests
  clearCollection()


  "TaskRepository" should {

    // Clear collection before each test
    clearCollection()

    val task1 = models.Task(None, "Task One", "This is a comment", false)
    val task2 = models.Task(None, "Task Two", "Another comment", false)
    val task3 = models.Task(None, "Task Three", "Third comment", true)

    taskService.create(task1)
    taskService.create(task2)
    taskService.create(task3)

    "return all tasks" in {
      val allTasks = taskService.all()
      allTasks must have length(3)
      
      // Since MongoDB doesn't guarantee order, we need to find tasks by name
      val foundTask1 = allTasks.find(_.name == "Task One").get
      val foundTask2 = allTasks.find(_.name == "Task Two").get
      val foundTask3 = allTasks.find(_.name == "Task Three").get
      
      foundTask1.comments must equal("This is a comment")
      foundTask1.completed must equal(false)
      
      foundTask2.comments must equal("Another comment")
      foundTask2.completed must equal(false)
      
      foundTask3.comments must equal("Third comment")
      foundTask3.completed must equal(true)
    }

    "retrieve a task by id" in {
      // First get all tasks to find a valid ID
      val allTasks = taskService.all()
      val taskId = allTasks.find(_.name == "Task One").get.id.get
      
      val aTask = taskService.getById(taskId)
      val task = aTask.get
      task.name must equal("Task One")
      task.comments must equal("This is a comment")
      task.completed must equal(false)
    }

    "create a task" in {
      val beforeTasks = taskService.all()
      val initialCount = beforeTasks.length

      val newTask = models.Task(None, "New Task", "A new comment", false)
      taskService.create(newTask)

      val afterTasks = taskService.all()
      afterTasks must have length(initialCount + 1)
      
      // Verify the new task exists
      afterTasks.exists(_.name == "New Task") must be(true)
    }

    "update a task" in {
      // First get all tasks to find a valid ID
      val allTasks = taskService.all()
      val taskToUpdate = allTasks.find(_.name == "Task One").get
      val updatedTask = taskToUpdate.copy(name = "Updated Name")
      
      taskService.update(taskToUpdate.id.get, updatedTask)

      val afterTasks = taskService.all()
      val updatedTaskResult = afterTasks.find(_.id == taskToUpdate.id).get
      updatedTaskResult.name must equal("Updated Name")
    }

    "delete a task" in {
      val beforeTasks = taskService.all()
      val initialCount = beforeTasks.length
      
      // Find a task to delete
      val taskToDelete = beforeTasks.find(_.name == "Task Two").get
      
      taskService.delete(taskToDelete.id.get)

      val afterTasks = taskService.all()
      afterTasks must have length(initialCount - 1)
      
      // Verify the task no longer exists
      afterTasks.exists(_.name == "Task Two") must be(false)
    }

  }
}
