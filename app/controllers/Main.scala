package controllers

import javax.inject._
import play.api.mvc._
import play.api.i18n.I18nSupport

@Singleton
class Main @Inject()(val controllerComponents: ControllerComponents) extends BaseController with I18nSupport {
  def index() = Action { implicit request: Request[AnyContent] =>
    Redirect(routes.TaskController.index)
  }
} 