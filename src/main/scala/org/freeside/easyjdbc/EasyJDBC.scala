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
import org.slf4j.LoggerFactory
import java.util.Date

/**
 * Trait to add the following functionalities:
 * - automatic connection management, even on wrapped calls
 * - auto rollback on errors
 * - inline parameter setting using the correct types
 * - fetchOne method for 0 or 1 result records using Option type
 * - lazy or prefetched resultsets
 * - enriched resultset with auto counting using next* methods
 *
 * Basic usage:
 * - implement the trait
 * - set the connectionFactory wiring your connection pool
 * - optionally change the default connectionCleaner and errorHandler
 * - use the sql* methods
 *
 * For more convenient usage, create your own trait with the proper configuration and use that all around.
 *
 * @author kjozsa
 */
trait EasyJDBC {
  /** how to get a connection */
  val connectionFactory: () => Connection

  /** how to get rid of connection */
  var connectionCleaner: Connection => Unit = { connection =>
    try {
      connection.close
    } catch {
      case e => logger.warn("failed to close connection", e)
    }
  }

  /** how to manage errors */
  var errorHandler: Throwable => Throwable = { e => e }

  private val logger = LoggerFactory.getLogger(getClass)

  private[easyjdbc] val threadConnectionManager = new ThreadLocal[ConnectionManager] {
    override def initialValue = new ConnectionManager(connectionFactory, connectionCleaner)
  }

  private def borrowConnection = threadConnectionManager.get.borrow
  private def returnConnection = threadConnectionManager.get.back

  /** execute a block using a new or threadlocal connection */
  def withConnection[T](executeBlock: java.sql.Connection => T): T = {
    val connection = borrowConnection
    try {
      executeBlock(connection)

    } catch {
      case e =>
        rollback(connection)
        throw errorHandler(e)

    } finally {
      returnConnection
    }
  }

  /** rollback, silence errors */
  private def rollback(connection: Connection) {
    try {
      if (!connection.getAutoCommit) connection.rollback
    } catch {
      case e => logger.error("failed to rollback connection", e)
    }
  }

  implicit def resultSet2EasyRS(rs: ResultSet) = new EasyResultSet(rs)
  implicit def easyRS2ResultSet(ers: EasyResultSet) = ers.rs

  implicit def ers2String(ers: EasyResultSet) = ers.nextString
  implicit def ers2Int(ers: EasyResultSet) = ers.nextInt

  /** execute an sql query and return an iterator of the processed list of results */
  def sqlQuery[T](sql: String, params: Any*)(resultProcessor: EasyResultSet => T): Iterator[T] = {
    withConnection { connection =>
      val statement = createStatement(connection, sql, params: _*)
      val results = statement.executeQuery

      val elements = new Iterator[ResultSet] {
        override def hasNext = results.next
        override def next = results
      }

      elements map (resultProcessor(_))
    }
  }

  /** execute an sql query and return all results fetched */
  def sqlFetch[T](sql: String, params: Any*)(resultProcessor: EasyResultSet => T): List[T] = {
    withConnection { connection =>
      val results = sqlQuery(sql, params: _*)(resultProcessor)
      results toList
    }
  }

  /** fetch 0 or 1 record with query */
  def sqlFetchOne[T](sql: String, params: Any*)(resultProcessor: EasyResultSet => T): Option[T] = {
    withConnection { connection =>
      val statement = createStatement(connection, sql, params: _*)
      val results = statement.executeQuery

      try {
        results.next match {
          case false => None
          case true => Some(resultProcessor(results))
        }
      } finally {
        if (results.next()) throw new IllegalStateException("Expected 0 or 1 but found more records")
      }
    }
  }

  /** fetch 0 or 1 record with query */
  def sqlFetchOneERS[T](sql: String, params: Any*): Option[EasyResultSet] = {
    withConnection { connection =>
      val statement = createStatement(connection, sql, params: _*)
      val results = statement.executeQuery

      try {
        results.next match {
          case false => None
          case true => Some(results)
        }
      } finally {
        if (results.next()) throw new IllegalStateException("Expected 0 or 1 but found more records")
      }
    }
  }

  /** execute an update */
  def sqlUpdate(sql: String, params: Any*) = {
    withConnection { connection =>
      val statement = createStatement(connection, sql, params: _*)
      statement.executeUpdate
    }
  }

  /** set the parameters on the prepared statement */
  private[easyjdbc] def createStatement(connection: Connection, sql: String, params: Any*) = {
    val marks = sql.count(_ == '?')
    require(marks == params.size, "Incorrect number of PreparedStatement parameters: " + marks + " vs " + params.size)

    val statement = connection.prepareStatement(sql)

    params.view.zipWithIndex foreach {
      case (value, index) =>
        val position = 1 + index

        addParameter(statement, position, value)
    }
    statement
  }

  /** add a parameter handling types correctly */
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
      case value: BigDecimal => statement.setBigDecimal(position, value.underlying())
      case value: Timestamp => statement.setTimestamp(position, value)
      case value: Date => statement.setTimestamp(position, new Timestamp(value.getTime))
      case value: String => statement.setString(position, value)
      case other => throw new UnsupportedOperationException("Unsupported parameter type of " + other)
    }
  }
}
