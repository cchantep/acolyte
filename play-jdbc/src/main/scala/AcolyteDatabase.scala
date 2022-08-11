// -*- mode: scala -*-
package acolyte.jdbc.play

import java.io.PrintWriter

import java.util.logging.Logger

import java.sql.{ Connection, DriverManager }

import acolyte.jdbc.{
  ConnectionHandler,
  Driver => AcolyteDriver,
  ResourceHandler,
  ScalaCompositeHandler
}
import play.api.db.Database

/**
 * Acolyte implementation for `play.api.db.Database`.
 *
 * @param handler the Acolyte handler
 * @param id the unique ID to register the `handler` for this DB instance
 */
final class AcolyteDatabase(
    handler: ScalaCompositeHandler,
    resourceHandler: ResourceHandler = new ResourceHandler.Default(),
    id: String = java.util.UUID.randomUUID().toString)
    extends Database
    with AcolyteDatabaseCompat { self =>

  AcolyteDriver.register(
    id,
    new ConnectionHandler.Default(handler, resourceHandler)
  )

  val name = s"acolyte-$id"

  val url = s"jdbc:acolyte:db?handler=$id"

  @SuppressWarnings(Array("NullAssignment"))
  object dataSource extends javax.sql.DataSource {
    private var timeout: Int = 0
    private var logWriter: PrintWriter = null

    override def getConnection: Connection = self.getConnection(false)

    override def getConnection(username: String, password: String): Connection =
      self.getConnection()

    def isWrapperFor(cls: Class[_]) = false

    def unwrap[T](cls: Class[T]): T = throw new java.sql.SQLException()

    override def setLogWriter(out: PrintWriter): Unit = {
      logWriter = out
    }

    override def getLoginTimeout: Int = timeout

    override def setLoginTimeout(seconds: Int): Unit = {
      timeout = seconds
    }

    override def getLogWriter: PrintWriter = logWriter

    override def getParentLogger: Logger = java.util.logging.Logger.getGlobal
  }

  def getConnection(autocommit: Boolean): Connection = {
    val con = DriverManager.getConnection(url)
    con.setAutoCommit(autocommit)
    con
  }

  override def getConnection(): Connection = getConnection(autocommit = false)

  override def withConnection[A](
      autocommit: Boolean
    )(block: Connection => A
    ): A = {
    lazy val con = getConnection(autocommit)

    try {
      block(con)
    } finally {
      con.close()
    }
  }

  override def withConnection[A](block: Connection => A): A =
    withConnection[A](autocommit = false)(block)

  def withTransaction[A](block: Connection => A): A =
    withConnection(autocommit = true)(block)

  @SuppressWarnings(Array("EmptyMethod"))
  def shutdown(): Unit = {}
}
