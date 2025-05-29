package controllers

import javax.inject._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import models.Task
import repositories.TaskRepository
import org.bson.Document
import org.bson.types.ObjectId

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's Task page.
 */
@Singleton
class TaskController @Inject()(
    val controllerComponents: ControllerComponents,
    taskRepository: TaskRepository
) extends BaseController with I18nSupport {

  val Home = Redirect(routes.TaskController.index)

  val taskForm = Form(
    mapping(
      "id" -> optional(text),
      "name" -> nonEmptyText,
      "comments" -> text,
      "completed" -> boolean,
      "labels" -> list(text)
    )(Task.apply)(Task.unapply)
  )

  def index() = Action { implicit request =>
    Ok(views.html.task.index(taskRepository.all(), taskForm))
  }

  def create() = Action { implicit request =>
    taskForm.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.task.index(taskRepository.all(), formWithErrors))
      },
      task => {
        // Convert comma-separated labels string to list
        val labelsString = request.body.asFormUrlEncoded.getOrElse(Map.empty).getOrElse("labels", Seq("")).head
        val labelsList = if (labelsString.trim.isEmpty) List.empty else labelsString.split(",").map(_.trim).toList
        val taskWithLabels = task.copy(labels = labelsList)
        taskRepository.create(taskWithLabels)
        Home
      }
    )
  }

  def edit(id: String) = Action { implicit request =>
    taskRepository.getById(id) match {
      case Some(task) =>
        Ok(views.html.task.editForm(id, taskForm.fill(task)))
      case None =>
        NotFound("Task not found")
    }
  }

  def update(id: String) = Action { implicit request =>
    taskForm.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.task.editForm(id, formWithErrors))
      },
      task => {
        // Convert comma-separated labels string to list
        val labelsString = request.body.asFormUrlEncoded.getOrElse(Map.empty).getOrElse("labels", Seq("")).head
        val labelsList = if (labelsString.trim.isEmpty) List.empty else labelsString.split(",").map(_.trim).toList
        val taskWithLabels = task.copy(labels = labelsList)
        taskRepository.update(id, taskWithLabels)
        Redirect(routes.TaskController.index)
      }
    )
  }

  def delete(id: String) = Action {
    taskRepository.delete(id)
    Redirect(routes.TaskController.index)
  }
}
