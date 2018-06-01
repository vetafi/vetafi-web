package models

import models.test.{ Nested, TestEnum, TestMessage }
import play.api.libs.json._

import scalapb.GeneratedEnum

class EnumFormat[T <: scalapb.GeneratedEnum](fromValue: (Int) => T) extends Format[T] {
  override def writes(value: T): JsValue = Json.toJson(value.value)

  override def reads(json: JsValue): JsResult[T] = JsSuccess(fromValue(json.as[Int]))
}

object serializers {
  implicit val testEnumFormat = new EnumFormat[TestEnum](TestEnum.fromValue)
  implicit val nestedJsonFormat = Json.format[Nested]
  implicit val testMessageJsonFormat = Json.format[TestMessage]
}
