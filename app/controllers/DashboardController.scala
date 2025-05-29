package controllers

import javax.inject._
import play.api.mvc._
import play.api.i18n.I18nSupport
import models.Task
import repositories.TaskRepository
import play.api.libs.json.Json

/**
 * This controller handles the dashboard visualization of tasks.
 */
@Singleton
class DashboardController @Inject()(
    val controllerComponents: ControllerComponents,
    taskRepository: TaskRepository
) extends BaseController with I18nSupport {

  def index() = Action { implicit request =>
    val tasks = taskRepository.all()
    
    // Calculate statistics for dashboard
    val completedTasks = tasks.count(_.completed)
    val pendingTasks = tasks.count(!_.completed)
    
    // Get all unique labels and count tasks per label
    val allLabels = tasks.flatMap(_.labels).distinct
    val tasksPerLabel = allLabels.map { label =>
      (label, tasks.count(_.labels.contains(label)))
    }
    
    // Get completed vs pending tasks per label
    val completedPerLabel = allLabels.map { label =>
      (label, tasks.count(t => t.labels.contains(label) && t.completed))
    }
    
    val pendingPerLabel = allLabels.map { label =>
      (label, tasks.count(t => t.labels.contains(label) && !t.completed))
    }
    
    Ok(views.html.dashboard.index(tasks, completedTasks, pendingTasks, 
                                 tasksPerLabel, completedPerLabel, pendingPerLabel))
  }
}