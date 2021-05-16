package io.github.ghostbuster91.sttp.client3.example

import _root_.sttp.client3._
import _root_.sttp.model._
import _root_.io.circe.Decoder
import _root_.io.circe.Encoder
import _root_.sttp.client3.circe.SttpCirceApi

trait CirceCodecs extends SttpCirceApi {
  implicit val personNameDecoder: Decoder[PersonName] =
    Decoder.decodeString.emap({
      case "bob" =>
        Right(PersonName.Bob)
      case "alice" =>
        Right(PersonName.Alice)
      case other =>
        Left("Unexpected value for enum:" + other)
    })
  implicit val personNameEncoder: Encoder[PersonName] =
    Encoder.encodeString.contramap({
      case PersonName.Bob   => "bob"
      case PersonName.Alice => "alice"
    })
  implicit val personDecoder: Decoder[Person] =
    Decoder.forProduct2("name", "age")(Person.apply)
  implicit val personEncoder: Encoder[Person] =
    Encoder.forProduct2("name", "age")(p => (p.name, p.age))
  implicit val organizationDecoder: Decoder[Organization] =
    Decoder.forProduct1("name")(Organization.apply)
  implicit val organizationEncoder: Encoder[Organization] =
    Encoder.forProduct1("name")(p => p.name)
  implicit val entityDecoder: Decoder[Entity] = List[Decoder[Entity]](
    Decoder[Person].asInstanceOf[Decoder[Entity]],
    Decoder[Organization].asInstanceOf[Decoder[Entity]]
  ).reduceLeft(_ or _)
  implicit val entityEncoder: Encoder[Entity] = Encoder.instance({
    case person: Person             => Encoder[Person].apply(person)
    case organization: Organization => Encoder[Organization].apply(organization)
  })
}
object CirceCodecs extends CirceCodecs

sealed trait PersonName
object PersonName {
  case object Bob extends PersonName()
  case object Alice extends PersonName()
}

sealed trait Entity {
  def name: PersonName
}
case class Organization(name: PersonName) extends Entity()
case class Person(name: PersonName, age: Int) extends Entity()

class DefaultApi(baseUrl: String, circeCodecs: CirceCodecs = CirceCodecs) {
  import circeCodecs._

  def getRoot(): Request[Entity, Any] = basicRequest
    .get(uri"$baseUrl")
    .response(
      fromMetadata(
        asJson[Entity].getRight,
        ConditionalResponseAs(
          _.code == StatusCode.unsafeApply(200),
          asJson[Entity].getRight
        )
      )
    )
}
