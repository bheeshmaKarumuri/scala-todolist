package controllers.api

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import repositories.TaskRepository
import org.scalatestplus.play.guice._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.ControllerComponents
import scala.concurrent.ExecutionContext
import play.api.libs.json._

class APITaskControllerSpec extends PlaySpec with GuiceOneAppPerSuite {

  val controllerComponents = app.injector.instanceOf[ControllerComponents]
  val configuration = app.injector.instanceOf[play.api.Configuration]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  def taskRepository: TaskRepository = new TaskRepository(configuration)
  def taskController: APITaskController = new APITaskController(controllerComponents, taskRepository)

  def clearCollection(): Unit = {
    val client = org.mongodb.scala.MongoClient("mongodb://localhost:27017")
    val database = client.getDatabase("todo")
    val collection = database.getCollection("tasks")
    import scala.concurrent.Await
    import scala.concurrent.duration._
    Await.result(collection.drop().toFuture(), 10.seconds)
  }

  "APITaskController index" should {
    "render the index view" in {
      clearCollection()
      val indexView = taskController.index().apply(FakeRequest(GET, "/api/tasks"))
      status(indexView) mustBe OK
      contentType(indexView) mustBe Some("application/json")
      contentAsString(indexView) must equal("[]")
    }
  }

  "TaskController create" should {
    "successfully create a task and redirect to the index view" in {
      clearCollection()
      val result = taskController.create().apply(
        FakeRequest(POST, "/api/tasks").withFormUrlEncodedBody("name" -> "Test Task", "comments" -> "Test Comments", "completed" -> "false")
      )
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      val bodyText: String = contentAsString(result)
      bodyText must include("Test Task")

      // verify new task shows up on index view
      val list = taskController.index()(FakeRequest())
      status(list) must equal(OK)
      contentType(list) mustBe Some("application/json")
      val listText: String = contentAsString(list)
      // Should be a JSON array with one element
      val jsArr = Json.parse(listText).as[JsArray]
      jsArr.value.count {
        case obj: JsObject =>
          obj.value.get("name").flatMap(_.asOpt[String]).contains("Test Task")
        case _ => false
      } mustBe 1
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
        FakeRequest(POST, "/api/tasks").withFormUrlEncodedBody("name" -> "Test Task", "comments" -> "Test Comments", "completed" -> "false")
      )
      status(createResult) mustBe OK
      val allTasks = taskRepository.all()
      val idToDelete = allTasks.find(_.name == "Test Task").flatMap(_.id).getOrElse("")
      val result = taskController.delete(idToDelete).apply(FakeRequest(POST, s"/api/tasks/$idToDelete/delete"))
      status(result) mustBe OK
      contentAsString(result) must equal("DELETED")

      // verify task does not show up on index view
      val list = taskController.index()(FakeRequest())
      status(list) must equal(OK)
      val listText: String = contentAsString(list)
      val jsArr = Json.parse(listText).as[JsArray]
      jsArr.value.count {
        case obj: JsObject =>
          obj.value.get("name").flatMap(_.asOpt[String]).contains("Test Task")
        case _ => false
      } mustBe 0
    }
  }

}
