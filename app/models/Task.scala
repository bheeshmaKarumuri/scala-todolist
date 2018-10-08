package models

import anorm._


case class Task(
    id: Option[Long] = None,
    name: String,
    comments: String,
    completed: Boolean)

object Task {
  implicit def toParameters: ToParameterList[Task] =
    Macro.toParameters[Task]
}
