/**
 *
 */
package freeside

import java.sql.Connection
import java.sql.Timestamp
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.PreparedStatement

/**
 * @author kjozsa
 */
class SQLHelper(
  implicit factory: SQLHelper.ConnectionFactory,
  errorHandler: SQLHelper.ErrorHandler = new DefaultErrorHandler) {

  println("using factory: " + factory + ", errorhandler: " + errorHandler)

  /** execute 'something' using a new or wrapped connection */
  def execute[T](executeBlock: Connection => T): T = {
    val connection = factory.connection
    try {
      executeBlock(connection)
    } catch {
      case e =>
        rollback(connection)
        throw errorHandler.prepare(e)
    }
  }

  private def rollback(connection: Connection) {
    try {
      if (!connection.getAutoCommit) connection.rollback
    } catch {
      case e => // silent 
    }
  }

  /** execute an sql query and process a list of results by records */
  def query[T](sql: String, params: Any*)(resultProcessor: ResultSet => T): List[T] = {
    execute { connection =>
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
  def queryOne[T](sql: String, params: Any*)(resultProcessor: ResultSet => T): Option[T] = {
    val results = query(sql, params)(resultProcessor)
    results.length match {
      case 0 => None
      case 1 => Some(results.head)
      case x => throw new IllegalStateException("More than one records found")
    }
  }

  /** execute an update */
  def update(sql: String, params: Any*) {
    execute { connection =>
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

object SQLHelper {
  trait ConnectionFactory {
    def connection: Connection
  }
  trait ErrorHandler {
    def prepare(e: Throwable): Throwable
  }

  class DefaultErrorHandler extends SQLHelper.ErrorHandler {
    override def prepare(e: Throwable) = {
      new RuntimeException(e)
    }
  }
}
