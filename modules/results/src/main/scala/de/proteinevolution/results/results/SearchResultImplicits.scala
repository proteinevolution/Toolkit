package de.proteinevolution.results.results

import io.circe._
import io.circe.syntax._

// https://github.com/circe/circe/issues/216

trait SearchResultImplicits {

  implicit val encodeDoubleOrIntOrString: Encoder[Either[Either[Double, Int], String]] =
    Encoder.instance(_.fold(_.fold(_.asJson, _.asJson), _.asJson))

  implicit val encodeDoubleOrString: Encoder[Either[Double, String]] =
    Encoder.instance(_.fold(_.asJson, _.asJson))
}

object SearchResultImplicits extends SearchResultImplicits
