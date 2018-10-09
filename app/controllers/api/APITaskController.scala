package controllers.api

import javax.inject._
import play.api._
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
class APITaskController @Inject()(taskService: TaskRepository, val cc: ControllerComponents) extends AbstractController(cc) with I18nSupport {

  private val logger = Logger(getClass)

  def index = Action { implicit request: Request[AnyContent] =>
    logger.trace("index: ")
    val tasks = taskService.all
    Ok(Json.toJson(tasks))
  }

  def show(id: Long) = Action { implicit request: Request[AnyContent] =>
    logger.trace("index: ")
    val task = taskService.getById(id)
    Ok(Json.toJson(task.get))
  }

}
