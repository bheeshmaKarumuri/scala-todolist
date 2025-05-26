package controllers.api

import javax.inject._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import models.Task
import repositories.TaskRepository
import play.api.libs.json.Json

/**
  * Takes HTTP requests and produces JSON.
  */
@Singleton
class APITaskController @Inject()(
    val controllerComponents: ControllerComponents,
    taskRepository: TaskRepository
) extends BaseController with I18nSupport {

  val taskForm = Form(
    mapping(
      "id" -> optional(text),
      "name" -> nonEmptyText,
      "comments" -> text,
      "completed" -> boolean
    )(Task.apply)(Task.unapply)
  )

  def index() = Action { implicit request =>
    val tasks = taskRepository.all()
    Ok(Json.toJson(tasks))
  }

  def show(id: String) = Action { implicit request =>
    taskRepository.getById(id) match {
      case Some(task) => Ok(Json.toJson(task))
      case None => NotFound("Task not found")
    }
  }

  def create() = Action { implicit request =>
    taskForm.bindFromRequest().fold(
      errors => BadRequest(errors.errorsAsJson),
      task => {
        taskRepository.create(task)
        Ok(Json.toJson(task))
      }
    )
  }

  def delete(id: String) = Action {
    taskRepository.delete(id)
    Ok("DELETED")
  }
}
