package de.proteinevolution.results.services

import io.circe.Json
import io.circe.parser._

trait AlignmentService {

  protected def parseAln(json: Json) = {

    for {
      result <- json.hcursor.downField("resultName")
    } yield {

    }

    val result =  json. // Alignment.parse((jsValue \ resultName).as[JsArray]).alignment
    val fas = numList.distinct.map { num =>
      ">" + result { num - 1 }.accession + "\n" + result { num - 1 }.seq + "\n"
    }

  }


}
