package acolyte.jdbc

import java.sql.{ SQLException, SQLFeatureNotSupportedException }

object BlobSpec extends org.specs2.mutable.Specification {
  "Binary Large OBject" title

  "Nil instance" should {
    "be zero-sized" in {
      Blob.Nil aka "Nil blob" must beLike {
        case blob => blob.length aka "size" must_== 0
      }
    }

    "has empty binary stream" in {
      Blob.Nil.getBinaryStream.read aka "binary stream #1" must_== -1 and (
        Blob.Nil.getBinaryStream(1, 2).read aka "binary stream #2" must_== -1
      )
    }

    "throw SQL exception accessing stream at position > 1" in {
      Blob.Nil.getBinaryStream(2, 1).
        aka("binary stream") must throwA[SQLException]("Invalid position: 2")

    }

    "has empty binary stream" in {
      Blob.Nil.getBytes(1, 3).length aka "bytes" must_== 0
    }

    "throw SQL exception with position > 1" in {
      Blob.Nil.getBytes(3, 1) aka "bytes" must throwA[SQLException](
        "Invalid position: 3"
      )

    }

    "throw SQL exception looking for invalid position" in {
      Blob.Nil.position(serialBlob, 3) aka "position" must throwA[SQLException](
        "Invalid offset: 3"
      ) and (Blob.Nil.position(testData, 5).
          aka("position") must throwA[SQLException]("Invalid offset: 5"))
    }

    "not find position" in {
      Blob.Nil.position(serialBlob, 0) aka "position" must_== -1L and (
        Blob.Nil.position(serialBlob, 1) aka "position" must_== -1L
      ) and (
          Blob.Nil.position(testData, 0) aka "position" must_== -1L
        ) and (
            Blob.Nil.position(testData, 1) aka "position" must_== -1L
          )
    }

    "truncate as no-op" in {
      (Blob.Nil.truncate(0) aka "truncate to 0" must not(throwA[SQLException])).
        and(Blob.Nil.truncate(1) aka "truncate to 1" must not(
          throwA[SQLException]
        ))
    }

    "not be writable" in {
      (Blob.Nil.setBinaryStream(1) aka "write request @ 1" must (
        throwA[SQLFeatureNotSupportedException]("Cannot write to empty BLOB")
      )).
        and(Blob.Nil.setBinaryStream(3) aka "write request @ 3" must (
          throwA[SQLException]("Invalid position: 3")
        ))
    }

    "not be set missing data" in {
      Blob.Nil.setBytes(1, null) aka "missing data" must (
        throwA[IllegalArgumentException]("No byte to be set")
      )
    }

    "throw SQL exception setting bytes at invalid position" in {
      Blob.Nil.setBytes(2, Array[Byte]()) aka "setting bytes" must (
        throwA[SQLException]("Invalid position: 2")
      )
    }

    "throw exception setting bytes with invalid length" in {
      Blob.Nil.setBytes(1, Array[Byte](), 0, -1) aka "setting bytes" must (
        throwA[IllegalArgumentException]("Invalid bytes length: -1")
      )
    }

    "throw exception setting bytes with length greater than data" in {
      Blob.Nil.setBytes(1, Array[Byte](), 0, 3) aka "setting bytes" must (
        throwA[IllegalArgumentException]("Fails to prepare binary data")
      )
    }
  }

  lazy val testData = s"Test: ${System identityHashCode this}" getBytes "UTF-8"
  lazy val serialBlob = new javax.sql.rowset.serial.SerialBlob(testData)
}
