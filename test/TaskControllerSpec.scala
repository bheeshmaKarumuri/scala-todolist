package controllers

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import repositories.TaskRepository
import org.scalatestplus.play.guice._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.ControllerComponents
import scala.concurrent.ExecutionContext

class TaskControllerSpec extends PlaySpec with GuiceOneAppPerSuite {
  val controllerComponents = app.injector.instanceOf[ControllerComponents]
  val configuration = app.injector.instanceOf[play.api.Configuration]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  def taskRepository: TaskRepository = new TaskRepository(configuration)
  def taskController: TaskController = new TaskController(controllerComponents, taskRepository)

  def clearCollection(): Unit = {
    val client = org.mongodb.scala.MongoClient("mongodb://localhost:27017")
    val database = client.getDatabase("todo")
    val collection = database.getCollection("tasks")
    import scala.concurrent.Await
    import scala.concurrent.duration._
    Await.result(collection.drop().toFuture(), 10.seconds)
  }

  "TaskController index" should {
    "render the index view" in {
      clearCollection()
      val indexView = taskController.index().apply(FakeRequest(GET, "/tasks"))
      status(indexView) mustBe OK
      contentType(indexView) mustBe Some("text/html")
    }

    "render the tasks view from the router" in {
      clearCollection()
      val request = FakeRequest(GET, "/tasks")
      val indexView = route(app, request).get
      status(indexView) mustBe OK
      contentType(indexView) mustBe Some("text/html")
    }
  }

  "TaskController create" should {
    "successfully create a task and redirect to the index view" in {
      clearCollection()
      val result = taskController.create().apply(
        FakeRequest(POST, "/tasks").withFormUrlEncodedBody("name" -> "Test Task", "comments" -> "Test Comments", "completed" -> "false")
      )
      status(result) mustBe 303
      redirectLocation(result) mustBe Some("/tasks")

      // verify new task shows up on index view
      val list = taskController.index()(FakeRequest())
      status(list) must equal(OK)
      val html = contentAsString(list)
      // Check that exactly one row in the table contains "Test Task"
      html.split("<tr>").count(_.contains("Test Task")) mustBe 1
    }

    "fail trying to create a task with no data passed" in {
      clearCollection()
      val badResult = taskController.create().apply(FakeRequest())
      status(badResult) must equal(BAD_REQUEST)
    }
  }

  "TaskController delete" should {
    "successfully delete a task and redirect to the index view" in {
      clearCollection()
      // Create a task to delete
      val createResult = taskController.create().apply(
        FakeRequest(POST, "/tasks").withFormUrlEncodedBody("name" -> "Test Task", "comments" -> "Test Comments", "completed" -> "false")
      )
      status(createResult) mustBe 303
      val allTasks = taskRepository.all()
      val idToDelete = allTasks.find(_.name == "Test Task").flatMap(_.id).getOrElse("")
      val result = taskController.delete(idToDelete).apply(FakeRequest(POST, s"/tasks/$idToDelete/delete"))
      status(result) mustBe 303
      redirectLocation(result) mustBe Some("/tasks")

      // verify task does not show up on index view
      val list = taskController.index()(FakeRequest())
      status(list) must equal(OK)
      val html = contentAsString(list)
      html.split("<tr>").count(_.contains("Test Task")) mustBe 0
    }
  }

}
