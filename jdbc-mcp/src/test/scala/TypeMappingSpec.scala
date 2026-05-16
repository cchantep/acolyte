package acolyte.jdbc.mcp

final class TypeMappingSpec extends org.specs2.mutable.Specification {
  "TypeMapping".title

  "map standard types" should {
    "VARCHAR to String" in {
      TypeMapping.mapSqlType("VARCHAR") must beSuccessfulTry("String")
    }

    "BIGINT to Long" in {
      TypeMapping.mapSqlType("BIGINT") must beSuccessfulTry("Long")
    }

    "INTEGER to Integer" in {
      TypeMapping.mapSqlType("INTEGER") must beSuccessfulTry("Integer")
    }

    "BOOLEAN to Boolean" in {
      TypeMapping.mapSqlType("BOOLEAN") must beSuccessfulTry("Boolean")
    }

    "DECIMAL to BigDecimal" in {
      TypeMapping.mapSqlType("DECIMAL") must beSuccessfulTry("BigDecimal")
    }

    "TIMESTAMP to java.time.Instant" in {
      TypeMapping.mapSqlType("TIMESTAMP") must beSuccessfulTry(
        "java.time.Instant"
      )
    }

    "BINARY to Array[Byte]" in {
      TypeMapping.mapSqlType("BINARY") must beSuccessfulTry("Array[Byte]")
    }
  }

  "handle case insensitivity" should {
    "lowercase varchar" in {
      TypeMapping.mapSqlType("varchar") must beSuccessfulTry("String")
    }

    "mixed case VarChar" in {
      TypeMapping.mapSqlType("VarChar") must beSuccessfulTry("String")
    }

    "uppercase CHAR" in {
      TypeMapping.mapSqlType("CHAR") must beSuccessfulTry("String")
    }
  }

  "handle ambiguous types" should {
    "NUMERIC maps to default (BigDecimal)" in {
      TypeMapping.mapSqlType("NUMERIC") must beSuccessfulTry("BigDecimal")
    }

    "DECIMAL maps to default (BigDecimal)" in {
      TypeMapping.mapSqlType("DECIMAL") must beSuccessfulTry("BigDecimal")
    }

    "identify and get defaults" in {
      TypeMapping.isAmbiguous("NUMERIC") must beTrue and {
        TypeMapping.isAmbiguous("DECIMAL") must beTrue
      } and {
        TypeMapping.getAmbiguousDefault("NUMERIC") must beSome("BigDecimal")
      } and {
        TypeMapping.getAmbiguousDefault("DECIMAL") must beSome("BigDecimal")
      }
    }
  }

  "handle unsupported types" should {
    "fail with clear error" in {
      TypeMapping.mapSqlType("GEOMETRY") must beFailedTry and {
        TypeMapping.mapSqlType("STRUCT") must beFailedTry
      } and {
        TypeMapping.mapSqlType("UNKNOWNTYPE123") must beFailedTry
      }
    }

    "error message contains proper guidance" in {
      TypeMapping.mapSqlType("GEOMETRY") must beFailedTry.which(
        _.getMessage must contain("Unsupported SQL type")
      )
    }
  }

  "support custom mappings" should {
    "override types with custom mappings" in {
      TypeMapping.mapSqlType(
        "VARCHAR",
        Some("CustomString")
      ) must beSuccessfulTry("CustomString") and {
        TypeMapping.mapSqlType("NUMERIC", Some("Int")) must beSuccessfulTry(
          "Int"
        )
      } and {
        TypeMapping.mapSqlType(
          "GEOMETRY",
          Some("String")
        ) must beSuccessfulTry("String")
      }
    }
  }

  "handle trimming and whitespace" should {
    "trim spaces and special chars" in {
      TypeMapping.mapSqlType("  VARCHAR  ") must beSuccessfulTry("String") and {
        TypeMapping.mapSqlType("\tVARCHAR\n") must beSuccessfulTry("String")
      }
    }
  }
}
