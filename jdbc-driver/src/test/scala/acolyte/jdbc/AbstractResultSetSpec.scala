package acolyte.jdbc

import java.sql.{ ResultSet, SQLException }

import org.specs2.mutable.Specification

final class AbstractResultSetSpec extends Specification {
  "Abstract resultset specification".title

  "Wrapping" should {
    "be valid for java.sql.ResultSet" in {
      defaultSet
        .isWrapperFor(classOf[java.sql.ResultSet])
        .aka("is wrapper for java.sql.ResultSet") must beTrue

    }

    "be unwrapped to java.sql.ResultSet" in {
      Option(defaultSet.unwrap(classOf[ResultSet]))
        .aka("unwrapped") must beSome[ResultSet]

    }
  }

  "Holdability" should {
    "be CLOSE_CURSORS_AT_COMMIT" in {
      defaultSet.getHoldability.aka(
        "holdability"
      ) must_=== ResultSet.CLOSE_CURSORS_AT_COMMIT

    }
  }

  "New resultset" should {
    "refuse invalid cursor name" in {
      lazy val rs = new AbstractResultSet(null) {}

      rs aka "ctor" must throwA[IllegalArgumentException]
    }

    "not be closed" in {
      defaultSet.isClosed aka "closed" must beFalse
    }

    "have no statement" in {
      defaultSet.getStatement aka "statement" must beNull
    }

    "not be updated/inserted/deleted" in {
      lazy val rs = defaultSet

      (rs.rowInserted aka "inserted" must beFalse)
        .and(rs.rowUpdated aka "updated" must beFalse)
        .and(rs.rowDeleted aka "deleted" must beFalse)

    }

    "have read-only concurrency" in {
      defaultSet.getConcurrency.aka(
        "concurrency"
      ) must_=== ResultSet.CONCUR_READ_ONLY

    }

    "be forward only" in {
      defaultSet.getType aka "type" must_=== ResultSet.TYPE_FORWARD_ONLY
    }

    "have a cursor name" in {
      defaultSet.getCursorName aka "cursor name" must not(beNull)
    }

    "have no warnings" in {
      defaultSet.getWarnings aka "warnings" must beNull
    }
  }

  "Fetch size" should {
    "initially be zero" in {
      defaultSet.getFetchSize aka "size" must_=== 0
    }

    "be properly set" in {
      lazy val rs = defaultSet
      rs.setFetchSize(2)

      rs.getFetchSize aka "size" must_=== 2
    }
  }

  "Fetch direction" should {
    "initially be FETCH_FORWARD" in {
      defaultSet.getFetchDirection.aka(
        "direction"
      ) must_=== ResultSet.FETCH_FORWARD

    }

    "fail to be set when not scrollable" in {
      defaultSet
        .setFetchDirection(ResultSet.FETCH_REVERSE)
        .aka("setting fetch direction") must throwA[SQLException](
        "Type of result set is forward only"
      ) and (defaultSet
        .setFetchDirection(ResultSet.FETCH_UNKNOWN)
        .aka("setting fetch direction") must throwA[SQLException](
        "Type of result set is forward only"
      ))
    }

    "be property set on scrollable set" >> {
      "reverse" in {
        lazy val rs = scrollInsensitiveSet

        rs.setFetchDirection(ResultSet.FETCH_REVERSE)

        rs.getFetchDirection aka "direction" must_=== ResultSet.FETCH_REVERSE
      }

      "unknown" in {
        lazy val rs = scrollInsensitiveSet

        rs.setFetchDirection(ResultSet.FETCH_UNKNOWN)

        rs.getFetchDirection aka "direction" must_=== ResultSet.FETCH_UNKNOWN
      }
    }
  }

  "Row" should {
    "initially be zero" in {
      defaultSet.getRow aka "row" must_=== 0
    }

    "not be moved backward (forward only)" in {
      defaultSet.previous() aka "move backward" must throwA[SQLException]
    }

    "be relatively moved" >> {
      "without change for 0" in {
        defaultSet.relative(0) aka "moved" must beTrue
      }

      "with failure for negative count" in {
        defaultSet.relative(-2) aka "backward move" must throwA[SQLException](
          message = "Backward move"
        )
      }

      "with failure when out-of bounds" in {
        lazy val rs = defaultSet
        (rs.relative(1) aka "forward move" must beFalse)
          .and(rs.getRow aka "row" must_=== 1)

      }

      "successfully" in {
        lazy val rs = defaultSet
        rs.setFetchSize(1)

        (rs.getRow aka "current row" must_=== 0)
          .and(rs.relative(1) aka "forward move" must beTrue)
          .and(rs.getRow aka "new row" must_=== 1)

      }
    }

    "be moved to next" >> {
      "successfully" in {
        lazy val rs = defaultSet
        rs.setFetchSize(1)

        (rs.getRow aka "current row" must_=== 0)
          .and(rs.next() aka "move to next" must beTrue)
          .and(rs.getRow aka "new row" must_=== 1)

      }
    }

    "be absolutely moved" >> {
      "without change for 0" in {
        defaultSet.absolute(0) aka "moved" must beTrue
      }

      "with failure for back position" in {
        lazy val rs = defaultSet
        rs.setFetchSize(1)
        rs.next()

        (rs.getRow aka "current row" must_=== 1).and(
          rs.absolute(0) aka "backward move" must throwA[SQLException](
            message = "Backward move"
          )
        )

      }

      "successfully forward" in {
        lazy val rs = defaultSet
        rs.setFetchSize(1)

        rs.absolute(1) aka "forward move to 1" must beTrue
      }

      "to last row 0" in {
        lazy val rs = defaultSet

        (rs.absolute(-1) aka "move" must beTrue)
          .and(rs.getRow aka "new row" must_=== 0)

      }

      "to last row 1" in {
        lazy val rs = defaultSet
        rs.setFetchSize(1)

        (rs.absolute(-1) aka "move" must beTrue)
          .and(rs.getRow aka "new row" must_=== 1)

      }

      "after last 0" in {
        lazy val rs = defaultSet

        (rs.absolute(1) aka "move" must beFalse)
          .and(rs.isAfterLast aka "after last" must beTrue)
      }

      "after last 1" in {
        lazy val rs = defaultSet
        rs.setFetchSize(1)

        (rs.absolute(2) aka "move" must beFalse)
          .and(rs.isAfterLast aka "after last" must beTrue)
      }
    }

    "be moved before first" >> {
      "throwing exception" in {
        lazy val rs = defaultSet

        rs.beforeFirst aka "moving before first" must throwA[SQLException](
          message = "Type of result set is forward only"
        )
      }

      "without change if scrollable" in {
        lazy val rs = scrollInsensitiveSet
        rs.beforeFirst

        rs.getRow aka "row" must_=== 0 and (rs.isBeforeFirst aka "before first" must beTrue)
      }

      "with failure when backward and not scrollable" in {
        lazy val rs = defaultSet
        rs.setFetchSize(1)

        (rs.first aka "move first" must beTrue)
          .and(rs.getRow aka "row" must_=== 1)
          .and(
            rs.beforeFirst aka "before first" must throwA[SQLException](
              message = "Type of result set is forward only"
            )
          )

      }
    }

    "be moved to first" >> {
      "without change" in {
        lazy val rs = defaultSet
        (rs.first aka "first" must beFalse)
          .and(rs.getRow aka "row" must_=== 1)
          .and(rs.isAfterLast aka "after last" must beTrue)
      }

      "at 1" in {
        lazy val rs = defaultSet
        rs.setFetchSize(1)

        (rs.first aka "first" must beTrue)
          .and(rs.getRow aka "row" must_=== 1)
          .and(rs.isOn aka "on" must beTrue)
      }

      "with failure when backward" in {
        lazy val rs = defaultSet
        rs.setFetchSize(2)

        (rs.absolute(2) aka "forward move" must beTrue)
          .and(rs.getRow aka "row" must_=== 2)
          .and(
            rs.first aka "backward first" must throwA[SQLException](
              message = "Backward move"
            )
          )

      }
    }

    "be moved to last" >> {
      "without change" in {
        lazy val rs = defaultSet
        rs.last aka "last" must beTrue and (rs.getRow aka "row" must_=== 0)
      }

      "at 1" in {
        lazy val rs = defaultSet
        rs.setFetchSize(1)

        rs.last aka "last" must beTrue and (rs.getRow aka "row" must_=== 1)
      }
    }

    "be moved to after last" >> {
      "with failure when not scrollable" in {
        defaultSet.afterLast aka "moving after last" must throwA[SQLException](
          "Type of result set is forward only"
        )
      }

      "at 2" in {
        lazy val rs = scrollInsensitiveSet
        rs.setFetchSize(1)
        rs.afterLast()

        rs.getRow aka "row" must_=== 2
      }
    }
  }

  "Column update" should {
    lazy val rs = defaultSet

    "not be supported for simple types" in {
      (rs
        .updateNull(0)
        .aka("update") must throwA[UnsupportedOperationException])
        .and(
          rs.updateBoolean(0, true)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateByte(0, 1.toByte)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateShort(0, 1.toShort)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateInt(0, 1)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateLong(0, 1.toLong)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateFloat(0, 1.toFloat)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateDouble(0, 1.toDouble)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateBigDecimal(0, new java.math.BigDecimal("1"))
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateString(0, "val")
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateBytes(0, Array[Byte]())
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateDate(0, null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateTime(0, null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateTimestamp(0, null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateObject(0, null, 0)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateObject(0, null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateNull("col")
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateBoolean("col", true)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateByte("col", 1.toByte)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateShort("col", 1.toShort)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateInt("col", 1)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateLong("col", 1.toLong)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateFloat("col", 1.toFloat)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateDouble("col", 1.toDouble)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateBigDecimal("col", new java.math.BigDecimal("1"))
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateString("col", "val")
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateBytes("col", Array[Byte]())
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateDate("col", null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateTime("col", null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateTimestamp("col", null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateObject("col", null, 0)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateObject("col", null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateRef(0, null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateRef("col", null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateArray(0, null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateArray("col", null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateRowId(0, null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateRowId("col", null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateNString(0, "val")
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateNString("col", "val")
            .aka("update") must throwA[UnsupportedOperationException]
        )

    }

    "not supported for streams" in {
      (rs
        .updateAsciiStream(0, null, 0)
        .aka("update") must throwA[UnsupportedOperationException])
        .and(
          rs.updateBinaryStream(0, null, 0)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateCharacterStream(0, null, 0)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateAsciiStream("col", null, 0)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateBinaryStream("col", null, 0)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateCharacterStream("col", null, 0)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateBlob(0, null.asInstanceOf[java.sql.Blob])
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateBlob("col", null.asInstanceOf[java.sql.Blob])
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateClob(0, null.asInstanceOf[java.sql.Clob])
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateClob("col", null.asInstanceOf[java.sql.Clob])
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateNClob(0, null.asInstanceOf[java.sql.NClob])
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateNClob("col", null.asInstanceOf[java.sql.NClob])
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateSQLXML(0, null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateSQLXML("col", null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateNCharacterStream(0, null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateNCharacterStream("col", null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateBinaryStream(0, null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateBinaryStream("col", null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateCharacterStream(0, null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateCharacterStream("col", null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateAsciiStream(0, null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateAsciiStream("col", null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateBlob(0, null.asInstanceOf[java.io.InputStream])
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateBlob("col", null.asInstanceOf[java.io.InputStream])
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateClob(0, null.asInstanceOf[java.io.Reader])
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateClob("col", null.asInstanceOf[java.io.Reader])
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateNClob(0, null.asInstanceOf[java.io.Reader])
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateNClob("col", null.asInstanceOf[java.io.Reader])
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateNCharacterStream(0, null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateNCharacterStream("col", null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateBinaryStream(0, null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateBinaryStream("col", null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateCharacterStream(0, null, 0)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateCharacterStream("col", null, 0)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateAsciiStream("col", null)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateBlob(0, null, 0)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateBlob("col", null, 0)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateClob(0, null, 0)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateClob("col", null, 0)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateNClob(0, null, 0)
            .aka("update") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.updateNClob("col", null, 0)
            .aka("update") must throwA[UnsupportedOperationException]
        )

    }
  }

  "Row change" should {
    lazy val rs = defaultSet

    "not be supported" in {
      (rs.updateRow() aka "update" must throwA[UnsupportedOperationException])
        .and(
          rs.insertRow()
            .aka("insert") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.deleteRow()
            .aka("delete") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.refreshRow()
            .aka("refresh") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.cancelRowUpdates()
            .aka("cancel") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.moveToInsertRow()
            .aka("move-to-insert") must throwA[UnsupportedOperationException]
        )
        .and(
          rs.moveToCurrentRow()
            .aka("move-to-current") must throwA[UnsupportedOperationException]
        )

    }
  }

  "Closed set" should {
    "be marked" in {
      lazy val s = defaultSet
      s.close()

      (s.isClosed aka "closed" must beTrue).and(
        s.checkClosed aka "check" must throwA[SQLException](
          message = "Result set is closed"
        )
      )
    }
  }

  def defaultSet = new AbstractResultSet {}

  def scrollInsensitiveSet =
    new AbstractResultSet("si", ResultSet.TYPE_SCROLL_INSENSITIVE) {}
}
