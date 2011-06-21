/**
 *
 */
package org.freeside.jdbchelper

import java.sql.Connection
import java.sql.Timestamp
import java.sql.ResultSet
import javax.naming.InitialContext
import javax.sql.DataSource
import java.sql.PreparedStatement
import java.sql.Types

/**
 * @author kjozsa
 */
trait JDBCHelper {

  /** execute something using a new or threadlocal connection */
  def sqlExecute[T](executeBlock: Connection => T): T = {
    val connection = JDBCHelper.thread.get.connection
    try {
      executeBlock(connection)
    } catch {
      case e =>
        rollback(connection)
        throw JDBCHelper.errorHandler.handle(e)

    } finally {
      JDBCHelper.thread.get.back
    }
  }

  /** rollback, silence errors */
  private def rollback(connection: Connection) {
    try {
      if (!connection.getAutoCommit) connection.rollback
    } catch {
      case e => // silent 
    }
  }

  /** execute an sql query and process a list of results by records */
  def sqlQuery[T](sql: String, params: Any*)(resultProcessor: ResultSet => T): List[T] = {
    sqlExecute { connection =>
      val statement = prepareStatement(connection, sql, params)
      val results = statement.executeQuery

      var processed: List[T] = Nil
      while (results.next) {
        processed = resultProcessor(results) :: processed
      }
      processed.reverse
    }
  }

  /** query for 0 or 1 record */
  def sqlQueryOne[T](sql: String, params: Any*)(resultProcessor: ResultSet => T): Option[T] = {
    val results = sqlQuery(sql, params)(resultProcessor)
    results.length match {
      case 0 => None
      case 1 => Some(results.head)
      case x => throw new IllegalStateException("More than one records found")
    }
  }

  /** execute an update */
  def sqlUpdate(sql: String, params: Any*) {
    sqlExecute { connection =>
      val statement = prepareStatement(connection, sql, params)
      statement.executeQuery
    }
  }

  /** set the parameters on the prepared statement */
  private[jdbchelper] def prepareStatement(connection: Connection, sql: String, params: Any*) = {
    assert(sql.count(_ == '?') == params.size, "Incorrect number of PreparedStatement parameters")
    val statement = connection.prepareStatement(sql)

    params.view.zipWithIndex foreach {
      case (value, index) =>
        val position = 1 + index

        addParameter(statement, position, value)
    }
    statement
  }

  /** handle any other type */
  private def addParameter(statement: PreparedStatement, position: Int, value: Any) {
    value match {
      case null => statement.setNull(position, Types.NULL)
      case None => statement.setNull(position, Types.NULL)
      case Some(value) => addParameter(statement, position, value)

      case value: Boolean => statement.setBoolean(position, value)
      case value: Byte => statement.setByte(position, value)
      case value: Int => statement.setInt(position, value)
      case value: Long => statement.setLong(position, value)
      case value: Float => statement.setFloat(position, value)
      case value: Double => statement.setDouble(position, value)
      case value: Timestamp => statement.setTimestamp(position, value)
      //      case value: BigDecimal => statement.setBigDecimal(position, value) // @TODO convert to java bigdecimal
      case value: String => statement.setString(position, value)
      case other => throw new UnsupportedOperationException("Unsupported parameter type of " + other)
    }
  }

}

object JDBCHelper {
  var factory: ConnectionFactory = null
  var errorHandler: ErrorHandler = DefaultErrorHandler

  trait ConnectionFactory {
    def connection: Connection
  }
  trait ErrorHandler {
    def handle(e: Throwable): Throwable
  }

  object DefaultErrorHandler extends ErrorHandler {
    override def handle(e: Throwable) = {
      throw new RuntimeException(e)
    }
  }

  class JNDIConnectionFactory(jndiName: String) extends ConnectionFactory {
    override def connection: Connection = {
      new InitialContext().lookup(jndiName).asInstanceOf[DataSource].getConnection
    }

    override def toString = "JNDI connection factory to " + jndiName
  }

  private[jdbchelper] class ConnectionManager {
    private var depth = 0
    private lazy val cached: Connection = factory.connection

    def connection = {
      depth = depth + 1
      cached
    }

    def back {
      depth = depth - 1
      if (depth == 0) {
        cleanup(cached)
      }
    }

    private def cleanup(connection: Connection) {
      try {
        connection.close
      } catch {
        case e => // silent
      }
    }
  }

  private[jdbchelper] val thread = new ThreadLocal[ConnectionManager] {
    override def initialValue = new ConnectionManager
  }
}
