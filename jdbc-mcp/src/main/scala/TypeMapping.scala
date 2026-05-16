package acolyte.jdbc.mcp

import scala.util.{ Failure, Success, Try }

import scala.io.Source

import play.api.libs.json.{ JsObject, JsValue, Json }

object TypeMapping {
  private lazy val mappingData: Try[JsObject] = loadMappings

  private def loadMappings: Try[JsObject] = {
    Try {
      val resourceStream = getClass.getResourceAsStream("/type-mappings.json")
      val json = Source.fromInputStream(resourceStream).mkString

      Json.parse(json).as[JsObject]
    }
  }

  def mapSqlType(sqlType: String): Try[String] = mapSqlType(sqlType, None)

  def mapSqlType(
      sqlType: String,
      customMapping: Option[String]
    ): Try[String] = {
    customMapping match {
      case Some(mapping) => Success(mapping)

      case None =>
        mappingData.flatMap { data =>
          val standardMappings =
            (data \ "standard_mappings").as[Map[String, String]]

          val normalized = sqlType.trim.toUpperCase

          standardMappings.get(normalized) match {
            case Some(acolyteType) => Success(acolyteType)

            case None =>
              checkAmbiguous(normalized) match {
                case Some(acolyteType) => Success(acolyteType)
                case None              => createUnsupportedTypeError(normalized)
              }
          }
        }
    }
  }

  private def checkAmbiguous(sqlType: String): Option[String] = {
    mappingData.toOption.flatMap { data =>
      (data \ "ambiguous_mappings")
        .asOpt[Map[String, JsValue]]
        .flatMap(_.get(sqlType))
        .flatMap { ambigConfig => (ambigConfig \ "default").asOpt[String] }
    }
  }

  private def createUnsupportedTypeError(sqlType: String): Try[String] = {
    val unsupported = mappingData.toOption
      .flatMap(data => (data \ "unsupported_types").asOpt[Seq[String]])
      .getOrElse(Seq.empty)

    if (unsupported.contains(sqlType)) {
      Failure(
        new IllegalArgumentException(
          s"Unsupported SQL type: $sqlType. This type cannot be automatically mapped. Please provide an explicit type mapping (e.g., Map[$sqlType -> String])"
        )
      )
    } else {
      Failure(
        new IllegalArgumentException(
          s"Unknown SQL type: $sqlType. Not found in standard or ambiguous mappings. Please provide an explicit type mapping."
        )
      )
    }
  }

  def isAmbiguous(sqlType: String): Boolean = {
    val normalized = sqlType.trim.toUpperCase

    mappingData.toOption.flatMap { data =>
      (data \ "ambiguous_mappings").asOpt[Map[String, JsValue]]
    }.exists(_.contains(normalized))
  }

  def getAmbiguousDefault(sqlType: String): Option[String] = {
    if (isAmbiguous(sqlType)) checkAmbiguous(sqlType.trim.toUpperCase)
    else None
  }
}
