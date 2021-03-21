package io.github.ghostbuster91.sttp.client3.example

import _root_.sttp.client3._
import _root_.sttp.model._
import _root_.sttp.client3.circe._
import _root_.io.circe.generic.auto._

case class Person(name: String, age: Int)

class Api(baseUrl: String) {
  def getRoot(id: Option[String], name: Int): Request[Person, Any] =
    basicRequest
      .get(uri"https://$baseUrl?id=$id&name=$name")
      .response(asJson[Person].getRight)
}
