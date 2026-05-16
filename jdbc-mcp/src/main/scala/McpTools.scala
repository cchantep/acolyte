package acolyte.jdbc.mcp

import java.nio.file.{ Files, Path, Paths, StandardCopyOption }

import java.util.{ concurrent, UUID }

import java.net.{ URL, URLClassLoader }

import java.sql.{ Connection, DriverManager }

import scala.util.{ Failure, Success, Try }

import play.api.libs.json.{
  JsArray,
  JsBoolean,
  JsNull,
  JsNumber,
  JsObject,
  JsString,
  JsValue,
  Json
}

final case class ConnectionInfo(
    id: String,
    url: String,
    user: String,
    password: String)

object McpTools {

  private[mcp] val connections =
    new concurrent.ConcurrentHashMap[String, Connection]()

  object ConnectTool extends JsonRpcHandler {

    private final case class DriverRegistryEntry(
        urlPrefix: String,
        driverClass: String,
        dependency: String)

    private final case class MavenDependency(
        group: String,
        artifact: String,
        version: String) {

      val coordinates: String = s"$group:$artifact:$version"

      val groupPath: String = group.replace('.', '/')

      val jarName: String = s"$artifact-$version.jar"

      val relativeJarPath: String = s"$groupPath/$artifact/$version/$jarName"
    }

    private final class DriverShim(delegate: java.sql.Driver)
        extends java.sql.Driver {
      def acceptsURL(url: String): Boolean = delegate.acceptsURL(url)

      def connect(url: String, info: java.util.Properties): Connection =
        delegate.connect(url, info)

      def getMajorVersion: Int = delegate.getMajorVersion
      def getMinorVersion: Int = delegate.getMinorVersion
      def jdbcCompliant: Boolean = delegate.jdbcCompliant
      def getParentLogger: java.util.logging.Logger = delegate.getParentLogger

      def getPropertyInfo(
          url: String,
          info: java.util.Properties
        ): Array[java.sql.DriverPropertyInfo] =
        delegate.getPropertyInfo(url, info)
    }

    private val mavenCentralBaseUrl = "https://repo1.maven.org/maven2"

    private val driverRegistry = List(
      DriverRegistryEntry(
        "jdbc:postgresql:",
        "org.postgresql.Driver",
        "org.postgresql:postgresql:42.7.1"
      ),
      DriverRegistryEntry(
        "jdbc:mysql:",
        "com.mysql.cj.jdbc.Driver",
        "com.mysql:mysql-connector-j:8.4.0"
      ),
      DriverRegistryEntry(
        "jdbc:mariadb:",
        "org.mariadb.jdbc.Driver",
        "org.mariadb.jdbc:mariadb-java-client:3.4.1"
      ),
      DriverRegistryEntry(
        "jdbc:oracle:",
        "oracle.jdbc.OracleDriver",
        "com.oracle.database.jdbc:ojdbc11:23.4.0.24.05"
      ),
      DriverRegistryEntry(
        "jdbc:sqlserver:",
        "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        "com.microsoft.sqlserver:mssql-jdbc:12.8.1.jre11"
      ),
      DriverRegistryEntry(
        "jdbc:h2:",
        "org.h2.Driver",
        "com.h2database:h2:2.2.224"
      ),
      DriverRegistryEntry(
        "jdbc:sqlite:",
        "org.sqlite.JDBC",
        "org.xerial:sqlite-jdbc:3.46.1.3"
      )
    )

    private val loadedClassLoaders =
      new concurrent.ConcurrentHashMap[String, URLClassLoader]()

    private val registeredDriverClasses =
      new concurrent.ConcurrentHashMap[String, Boolean]()

    private val envPlaceholderPattern = "^\\$\\{([A-Za-z_][A-Za-z0-9_]*)\\}$".r

    private val envPrefixPattern = "^env:([A-Za-z_][A-Za-z0-9_]*)$".r

    def handle(params: JsValue): Try[JsObject] = {
      val rawUrl = (params \ "url").asOpt[String]
      val rawUser = (params \ "user").asOpt[String]
      val rawPassword = (params \ "password").asOpt[String]
      val rawRequestedDriver = (params \ "driver").asOpt[String]
      val rawRequestedDependency = (params \ "dependency").asOpt[String]
      val rawRepository = (params \ "repository").asOpt[String]

      val resolved = for {
        u <- rawUrl.fold(
          Failure(
            new IllegalArgumentException("Missing url parameter")
          ): Try[String]
        )(resolveEnvReference(_, "url"))
        resolvedUser <- resolveOptionalEnvReference(rawUser, "user")
        resolvedPassword <- resolveOptionalEnvReference(rawPassword, "password")
        resolvedRequestedDriver <- resolveOptionalEnvReference(
          rawRequestedDriver,
          "driver"
        )
        resolvedRequestedDependency <- resolveOptionalEnvReference(
          rawRequestedDependency,
          "dependency"
        )
        resolvedRepository <- resolveOptionalEnvReference(
          rawRepository,
          "repository"
        )
      } yield (
        u,
        resolvedUser.orNull,
        resolvedPassword.orNull,
        resolvedRequestedDriver,
        resolvedRequestedDependency,
        resolvedRepository.getOrElse(mavenCentralBaseUrl)
      )

      resolved.flatMap {
        case (
              u,
              user,
              password,
              requestedDriver,
              requestedDependency,
              repository
            ) =>
          val registryEntry = inferRegistryEntry(u)
          val driverClass = requestedDriver.orElse(registryEntry.map(_._1))

          val dependencyCoordinates =
            requestedDependency.orElse(registryEntry.map(_._2))

          val dependency = dependencyCoordinates.fold(
            Try(Option.empty[MavenDependency])
          )(dep => parseDependency(dep).map(Some(_)))

          driverClass.fold(
            Try(
              Json.obj(
                "error" -> s"Unable to infer JDBC driver from URL: $u. Please provide `driver` and `dependency` (group:artifact:version).",
                "status" -> "driver_missing",
                "url" -> u,
                "missingDependency" -> JsNull
              )
            )
          )(driver =>
            dependency.flatMap { dep =>
              ensureDriverAvailable(
                driverClass = driver,
                dependency = dep,
                repository = repository
              ).flatMap { loadSource =>
                Try {
                  val id = UUID.randomUUID().toString
                  val conn = DriverManager.getConnection(u, user, password)
                  val _ = connections.put(id, conn)

                  Json.obj(
                    "id" -> id,
                    "status" -> "connected",
                    "driver" -> driver,
                    "dependency" -> optionalStringValue(dep.map(_.coordinates)),
                    "driverSource" -> loadSource,
                    "securityWarnings" -> Json.arr(
                      "Use this MCP connection only against non-sensitive databases (local/dev/test), not production.",
                      "Never expose credentials in prompts, logs, screenshots, or committed files."
                    ),
                    "credentialGuidance" -> Json.obj(
                      "recommendation" -> "Use environment variables or a secrets manager and inject credentials at runtime.",
                      "avoid" -> Json.arr(
                        "hardcoded passwords",
                        "plaintext secrets in scripts"
                      )
                    )
                  )
                }.recover {
                  case e: java.sql.SQLException
                      if Option(e.getMessage)
                        .exists(_.contains("No suitable driver")) =>
                    Json.obj(
                      "error" -> s"No suitable driver found for URL: $u. Confirm driver class and dependency coordinates.",
                      "status" -> "driver_missing",
                      "driver" -> driver,
                      "missingDependency" -> optionalStringValue(
                        dep.map(_.coordinates)
                      )
                    )

                  case e: java.sql.SQLException =>
                    Json.obj(
                      "error" -> s"Failed to connect: ${e.getMessage}",
                      "status" -> "connection_failed",
                      "driver" -> driver
                    )
                }
              }
            }.recover {
              case e: IllegalArgumentException =>
                Json.obj(
                  "error" -> e.getMessage,
                  "status" -> "driver_missing",
                  "driver" -> driver,
                  "missingDependency" -> optionalStringValue(
                    dependency.toOption.flatten.map(_.coordinates)
                  )
                )

              case e: RuntimeException =>
                Json.obj(
                  "error" -> e.getMessage,
                  "status" -> "driver_missing",
                  "driver" -> driver,
                  "missingDependency" -> optionalStringValue(
                    dependency.toOption.flatten.map(_.coordinates)
                  )
                )
            }
          )
      }.recover {
        case e: IllegalArgumentException =>
          Json.obj(
            "error" -> e.getMessage,
            "status" -> "invalid_parameters"
          )
      }
    }

    private def resolveOptionalEnvReference(
        value: Option[String],
        parameterName: String
      ): Try[Option[String]] =
      value.fold(Try(Option.empty[String])) { raw =>
        resolveEnvReference(raw, parameterName).map(Some(_))
      }

    private def resolveEnvReference(
        value: String,
        parameterName: String
      ): Try[String] = value match {
      case envPlaceholderPattern(variable) =>
        resolveEnvironmentVariable(variable, parameterName)

      case envPrefixPattern(variable) =>
        resolveEnvironmentVariable(variable, parameterName)

      case plain => Try(plain)
    }

    private def resolveEnvironmentVariable(
        variable: String,
        parameterName: String
      ): Try[String] =
      sys.env
        .get(variable)
        .fold[Try[String]](
          Failure(
            new IllegalArgumentException(
              s"Missing environment variable '$variable' for connect parameter '$parameterName'."
            )
          )
        )(value => Success(value))

    private def inferRegistryEntry(url: String): Option[(String, String)] =
      driverRegistry.collectFirst {
        case DriverRegistryEntry(prefix, driver, dependency)
            if url.startsWith(prefix) =>
          driver -> dependency
      }

    private def loadDriverClass(driverClass: String): Try[Unit] =
      Try {
        val _ = Class.forName(driverClass)
        ()
      }

    private def parseDependency(value: String): Try[MavenDependency] =
      value.split(':').toList match {
        case group :: artifact :: version :: Nil
            if group.nonEmpty && artifact.nonEmpty && version.nonEmpty =>
          Try(
            MavenDependency(
              group = group,
              artifact = artifact,
              version = version
            )
          )
        case _ =>
          Failure(
            new IllegalArgumentException(
              s"Invalid dependency format: $value. Expected group:artifact:version"
            )
          )
      }

    private def ensureDriverAvailable(
        driverClass: String,
        dependency: Option[MavenDependency],
        repository: String
      ): Try[String] =
      loadDriverClass(driverClass)
        .map(_ => "classpath")
        .recoverWith {
          case _: ClassNotFoundException =>
            dependency.fold(Try("missing")) { dep =>
              downloadAndLoadDriver(
                dependency = dep,
                driverClass = driverClass,
                repository = repository
              ).map(_ => "maven")
            }
        }
        .flatMap {
          case "missing" =>
            Failure(
              new ClassNotFoundException(
                s"JDBC driver class not found: $driverClass. Please provide dependency coordinates (group:artifact:version)."
              )
            )

          case source =>
            Success(source)
        }
        .recoverWith {
          case e: ClassNotFoundException =>
            Failure(
              new RuntimeException(
                s"JDBC driver class not found: $driverClass. Please add dependency ${dependency.fold("<provide dependency>")(_.coordinates)} and restart the MCP server.",
                e
              )
            )

          case e: IllegalArgumentException =>
            Failure(e)

          case e: Exception =>
            Failure(
              new RuntimeException(
                s"Failed to load JDBC driver $driverClass from Maven: ${e.getMessage}",
                e
              )
            )
        }

    private def downloadAndLoadDriver(
        dependency: MavenDependency,
        driverClass: String,
        repository: String
      ): Try[Unit] = {
      val localJar = localJarPath(dependency)

      val localJarTry = if (Files.exists(localJar)) {
        Try(localJar)
      } else {
        downloadJar(dependency, repository, localJar).map(_ => localJar)
      }

      localJarTry.flatMap(path =>
        registerLoadedDriver(driverClass, dependency.coordinates, path)
      )
    }

    private def localJarPath(dependency: MavenDependency): Path =
      Paths.get(
        System.getProperty("user.home"),
        ".acolyte",
        "jdbc-drivers",
        dependency.groupPath,
        dependency.artifact,
        dependency.version,
        dependency.jarName
      )

    private def downloadJar(
        dependency: MavenDependency,
        repository: String,
        target: Path
      ): Try[Unit] = Try {
      val normalizedRepo = repository.stripSuffix("/")
      val sourceUrl = new URL(s"$normalizedRepo/${dependency.relativeJarPath}")
      val parent = target.getParent

      Files.createDirectories(parent)

      val temp = Files.createTempFile(parent, "driver-", ".jar")

      try {
        val stream = sourceUrl.openStream()

        try {
          Files.copy(stream, temp, StandardCopyOption.REPLACE_EXISTING)
        } finally {
          stream.close()
        }

        val _ = Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING)
      } finally {
        val _ = Files.deleteIfExists(temp)
      }
    }

    private def registerLoadedDriver(
        driverClass: String,
        cacheKey: String,
        jarPath: Path
      ): Try[Unit] = Try {
      val existing = Option(loadedClassLoaders.get(cacheKey))

      val loader = existing.fold {
        val created = new URLClassLoader(
          Array(jarPath.toUri.toURL),
          getClass.getClassLoader
        )

        val _ = loadedClassLoaders.putIfAbsent(cacheKey, created)

        Option(loadedClassLoaders.get(cacheKey)).getOrElse(created)
      }(identity)

      val loaded = Class.forName(driverClass, true, loader)

      val instance = loaded
        .getDeclaredConstructor()
        .newInstance()
        .asInstanceOf[java.sql.Driver]

      val wasRegistered =
        Option(registeredDriverClasses.putIfAbsent(driverClass, true)).isDefined

      if (!wasRegistered) {
        DriverManager.registerDriver(new DriverShim(instance))
      }
    }

    private def optionalStringValue(value: Option[String]): JsValue =
      value.fold[JsValue](JsNull)(JsString(_))
  }

  object DiscoverTool extends JsonRpcHandler {

    def handle(params: JsValue): Try[JsValue] = {
      val connId = (params \ "connectionId").asOpt[String]
      val tableName = (params \ "table").asOpt[String]

      (connId, tableName) match {
        case (Some(cid), Some(tbl)) =>
          Option(connections.get(cid)) match {
            case Some(conn) =>
              Try {
                val metadata = conn.getMetaData
                val rs = metadata.getColumns(null, null, tbl, null)
                val columns = Iterator
                  .continually(rs.next())
                  .takeWhile(identity)
                  .map { _ =>
                    Json.obj(
                      "name" -> rs.getString("COLUMN_NAME"),
                      "type" -> rs.getInt("DATA_TYPE")
                    )
                  }
                  .toList

                try {
                  Json.obj("table" -> tbl, "columns" -> columns)
                } finally {
                  rs.close()
                }
              }

            case None => Try(Json.obj("error" -> "Invalid connection"))
          }

        case _ => Try(Json.obj("error" -> "Invalid connection or table"))
      }
    }
  }

  object RecordTool extends JsonRpcHandler {

    def handle(params: JsValue): Try[JsObject] = {
      val connId = (params \ "connectionId").asOpt[String]
      val query = (params \ "query").asOpt[String]
      val confirmed = (params \ "confirmed").asOpt[Boolean].getOrElse(false)

      query.fold(Try(Json.obj("error" -> "Invalid query"))) { q =>
        if (confirmed && connId.isDefined) {
          executeConfirmedQuery(connId, q)
        } else {
          extractAndConfirm(q)
        }
      }
    }

    private def extractAndConfirm(query: String): Try[JsObject] =
      QueryAnalyzer.analyzeQuery(query).map { structure =>
        Json.obj(
          "status" -> "awaiting_confirmation",
          "query" -> query,
          "extracted" -> Json.obj(
            "selectFields" -> structure.selectFields,
            "whereConditions" -> Json.toJson(
              structure.whereConditions.map(c =>
                Json.obj(
                  "field" -> c.field,
                  "operator" -> c.operator,
                  "placeholder" -> c.placeholder
                )
              )
            ),
            "parameterCount" -> structure.parameterCount
          ),
          "message" -> s"Extracted ${structure.selectFields.length} field(s), ${structure.whereConditions.length} condition(s), ${structure.parameterCount} parameter(s). Please confirm this extraction is correct before execution."
        )
      }

    private def executeConfirmedQuery(
        connId: Option[String],
        query: String
      ): Try[JsObject] =
      connId.fold(
        Try(Json.obj("error" -> "Missing connectionId for execution"))
      )(cid =>
        Option(connections.get(cid)).fold(
          Try(Json.obj("error" -> "Invalid connection"))
        )(conn =>
          QueryExecutor.executeQuery(conn, query, List(List.empty)).map {
            executions =>
              val columns =
                executions.headOption.fold(List.empty[QueryColumn])(_.columns)
              val useStringList = canUseStringList(columns)
              val rows = executions.flatMap(_.rows).map(renderRow)

              Json.obj(
                "status" -> "executed",
                "query" -> query,
                "executionCount" -> executions.length,
                "totalRows" -> executions.map(_.rowCount).sum,
                "columns" -> Json.toJson(columns.map(renderColumn)),
                "dslHints" -> Json.obj(
                  "recommendedRowListFactory" -> s"rowList${columns.length}",
                  "useStringList" -> useStringList,
                  "rule" -> (if (useStringList) {
                               "Use RowLists.stringList only when every selected column is textual."
                             } else {
                               "Use RowLists.rowListN with typed columns; do not use RowLists.stringList for mixed or non-text columns."
                             })
                ),
                "rows" -> rows
              )
          }
        )
      )

    private def renderColumn(column: QueryColumn): JsObject =
      Json.obj(
        "index" -> column.index,
        "name" -> column.name,
        "jdbcType" -> column.jdbcType,
        "jdbcTypeName" -> column.jdbcTypeName,
        "javaClassName" -> column.javaClassName
      )

    private def canUseStringList(columns: List[QueryColumn]): Boolean =
      columns.nonEmpty && columns.forall(isTextualColumn)

    private def isTextualColumn(column: QueryColumn): Boolean =
      column.jdbcType match {
        case java.sql.Types.CHAR | java.sql.Types.VARCHAR |
            java.sql.Types.LONGVARCHAR | java.sql.Types.NCHAR |
            java.sql.Types.NVARCHAR | java.sql.Types.LONGNVARCHAR |
            java.sql.Types.CLOB | java.sql.Types.NCLOB =>
          true

        case _ => false
      }

    private def renderRow(row: List[Any]): JsArray =
      JsArray(row.map(renderCell))

    private def renderCell(value: Any): JsValue = value match {
      case null                     => JsNull
      case s: String                => JsString(s)
      case i: Int                   => JsNumber(i)
      case l: Long                  => JsNumber(l)
      case d: Double                => JsNumber(BigDecimal(d))
      case f: Float                 => JsNumber(BigDecimal.decimal(f.toDouble))
      case b: Boolean               => JsBoolean(b)
      case bd: java.math.BigDecimal => JsNumber(BigDecimal(bd))
      case other                    => JsString(other.toString)
    }
  }

  object CloseTool extends JsonRpcHandler {

    def handle(params: JsValue): Try[JsObject] = {
      val connId = (params \ "connectionId").asOpt[String]

      connId match {
        case Some(cid) =>
          Option(connections.get(cid)) match {
            case Some(conn) =>
              Try {
                conn.close()
                connections.remove(cid)

                Json.obj("status" -> "closed", "connectionId" -> cid)
              }

            case None => Try(Json.obj("error" -> "Invalid connection"))
          }

        case None => Try(Json.obj("error" -> "Missing connectionId"))
      }
    }
  }
}
