/**
 *
 */
package org.freeside.jdbchelper

import java.sql.Connection
import java.sql.Timestamp
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.PreparedStatement
import freeside.JNDIConnectionFactory
import javax.naming.InitialContext
import javax.naming.Context
import javax.sql.DataSource

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
      val results = statement.executeQuery;

      var processed: List[T] = Nil
      while (results.next()) {
        processed = resultProcessor(results) :: processed
      }
      processed
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
      statement executeQuery
    }
  }

  /** set the parameters on the prepared statement */
  private def prepareStatement(connection: Connection, sql: String, params: Any*) = {
    val statement = connection.prepareStatement(sql);

    params.view.zipWithIndex foreach {
      case (param, index) =>
        val position = 1 + index

        param match {
          case param: Boolean => statement.setBoolean(position, param)
          case param: Byte => statement.setByte(position, param)
          case param: Int => statement.setInt(position, param)
          case param: Long => statement.setLong(position, param)
          case param: Float => statement.setFloat(position, param)
          case param: Double => statement.setDouble(position, param)
          case param: Timestamp => statement.setTimestamp(position, param)
          //          case param: BigDecimal => statement.setBigDecimal(position, param) @TODO turn bigdecimal to java 
          case param: String => statement.setString(position, param)
          case param => throw new UnsupportedOperationException("Unsupported parameter type of " + param)
        }
    }
    statement
  }
}

object JDBCHelper {
  var factory: ConnectionFactory = null
  var errorHandler: ErrorHandler = new DefaultErrorHandler

  trait ConnectionFactory {
    def connection: Connection
  }
  trait ErrorHandler {
    def handle(e: Throwable): Throwable
  }

  class DefaultErrorHandler extends ErrorHandler {
    override def handle(e: Throwable) = {
      throw new RuntimeException(e)
    }
  }

  class JNDIConnectionFactory(jndiName: String) extends ConnectionFactory {
    override def connection: Connection = {
      new InitialContext().lookup(jndiName).asInstanceOf[DataSource].getConnection;
    }

    override def toString = "JNDI connection factory to " + jndiName
  }

  class ConnectionDepth {
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

  val thread = new ThreadLocal[ConnectionDepth] {
    override def initialValue = new ConnectionDepth
  }
}