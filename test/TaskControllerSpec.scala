package controllers

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import repositories.TaskRepository
import org.scalatestplus.play.guice._


class TaskControllerSpec extends PlaySpec with GuiceOneAppPerSuite {

  def taskController = app.injector.instanceOf(classOf[TaskController])

  "TaskController GET" should {

    "render the tasks view" in {
      val tasksView = taskController.tasks().apply(FakeRequest(GET, "/tasks"))
      status(tasksView) mustBe OK
      contentType(tasksView) mustBe Some("text/html")
    }

    "render the tasks view from the router" in {
      val request = FakeRequest(GET, "/tasks")
      val tasksView = route(app, request).get
      status(tasksView) mustBe OK
      contentType(tasksView) mustBe Some("text/html")
    }
  }

}
