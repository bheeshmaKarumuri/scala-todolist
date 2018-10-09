package controllers

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.db.Databases
import play.api.db.evolutions._
import repositories.TaskRepository
import org.scalatestplus.play.guice._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{ AbstractController, ControllerComponents }
import scala.concurrent.ExecutionContext.Implicits.global



class TaskControllerSpec extends PlaySpec with GuiceOneAppPerSuite {

  val database = Databases(
    driver = "org.postgresql.Driver",
    url = "jdbc:postgresql://localhost:5432/scala_todo_test"
  )
  Evolutions.cleanupEvolutions(database)
  Evolutions.applyEvolutions(database)

  val injector = new GuiceApplicationBuilder().injector()
  val controllerComponents = injector.instanceOf[ControllerComponents]

  def taskService: TaskRepository = new TaskRepository(database)
  def taskController: TaskController = new TaskController(taskService, controllerComponents)


  "TaskController index" should {

    "render the index view" in {
      val indexView = taskController.index().apply(FakeRequest(GET, "/tasks"))
      status(indexView) mustBe OK
      contentType(indexView) mustBe Some("text/html")
    }

    "render the tasks view from the router" in {
      val request = FakeRequest(GET, "/tasks")
      val indexView = route(app, request).get
      status(indexView) mustBe OK
      contentType(indexView) mustBe Some("text/html")
    }
  }

  "TaskController create" should {

    "successfully create a task and redirect to the index view" in {
      val result = taskController.create(
        FakeRequest(POST, "/tasks").withFormUrlEncodedBody("name" -> "Test Task", "comments" -> "Test Comments", "completed" -> "false")
      )
      status(result) mustBe 303
      redirectLocation(result) mustBe Some("/tasks")
      flash(result).get("success") mustBe Some("Task Test Task has been created")
    }

    "fail trying to create a task with no data passed" in {
      val badResult = taskController.create(FakeRequest())
      status(badResult) must equal(BAD_REQUEST)
    }

  }

}
