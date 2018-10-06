package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import models.Task
import play.api.i18n.I18nSupport

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val cc: ControllerComponents) extends AbstractController(cc) with I18nSupport {

  val taskForm = Form(
    "label" -> nonEmptyText
  )

  def index = Action { implicit request: Request[AnyContent] =>
    Redirect(routes.HomeController.tasks)
  }

  def tasks = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index(Task.all(), taskForm))
  }
  def newTask = TODO

  def deleteTask(id: Long) = TODO
}
