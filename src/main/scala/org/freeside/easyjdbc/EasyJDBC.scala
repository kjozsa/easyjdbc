/**
 *
 */
package org.freeside.easyjdbc

import java.sql.Connection
import java.sql.Timestamp
import java.sql.ResultSet
import javax.naming.InitialContext
import javax.sql.DataSource
import java.sql.PreparedStatement
import java.sql.Types
import collection.Iterator

/**
 * @author kjozsa
 */
trait EasyJDBC {

  /**execute something using a new or threadlocal connection */
  def sqlExecute[T](executeBlock: Connection => T): T = {
    val connection = EasyJDBC.thread.get.connection
    try {
      executeBlock(connection)
    } catch {
      case e =>
        rollback(connection)
        throw EasyJDBC.errorHandler.handle(e)

    } finally {
      EasyJDBC.thread.get.back
    }
  }

  /**rollback, silence errors */
  private def rollback(connection: Connection) {
    try {
      if (!connection.getAutoCommit) connection.rollback
    } catch {
      case e => // silent 
    }
  }

  /**execute an sql query and process a list of results by records */
  def sqlQuery[T](sql: String, params: Any*)(resultProcessor: ResultSet => T): Iterator[T] = {
    implicit def iterableResultSet(rs: ResultSet) = {
      new Iterator[ResultSet] {
        override def hasNext = rs.next
        override def next = rs
      }
    }

    sqlExecute { connection =>
      val statement = prepareStatement(connection, sql, params: _*)
      val results = statement.executeQuery

      results.map(resultProcessor(_))
    }
  }

  /**query for 0 or 1 record */
  def sqlQueryOne[T](sql: String, params: Any*)(resultProcessor: ResultSet => T): Option[T] = {
    val results = sqlQuery(sql, params)(resultProcessor)
    results.length match {
      case 0 => None
      case 1 => Some(results.next)
      case x => throw new IllegalStateException("More than one records found")
    }
  }

  /**execute an update */
  def sqlUpdate(sql: String, params: Any*) {
    sqlExecute { connection =>
      val statement = prepareStatement(connection, sql, params: _*)
      statement.executeQuery
    }
  }

  /**set the parameters on the prepared statement */
  private[easyjdbc] def prepareStatement(connection: Connection, sql: String, params: Any*) = {
    val marks = sql.count(_ == '?')
    assert(marks == params.size, "Incorrect number of PreparedStatement parameters: " + marks + " vs " + params.size)

    val statement = connection.prepareStatement(sql)

    params.view.zipWithIndex foreach {
      case (value, index) =>
        val position = 1 + index

        addParameter(statement, position, value)
    }
    statement
  }

  /**handle any other type */
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


object EasyJDBC {
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
      throw e
    }
  }

  class JNDIConnectionFactory(jndiName: String) extends ConnectionFactory {
    override def connection: Connection = {
      new InitialContext().lookup(jndiName).asInstanceOf[DataSource].getConnection
    }

    override def toString = "JNDI connection factory to " + jndiName
  }

  private[easyjdbc] class ConnectionManager {
    private var depth = 0
    private lazy val cached: Connection = factory.connection

    if (factory == null) throw new IllegalArgumentException("No factory configured for EasyJDBC. Make sure you set the EasyJDBC.factory field!")

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

  private[easyjdbc] val thread = new ThreadLocal[ConnectionManager] {
    override def initialValue = new ConnectionManager
  }
}