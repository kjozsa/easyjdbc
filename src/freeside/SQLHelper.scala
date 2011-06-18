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
  def execute[T](sqlBlock: Connection => T): T = {
    try {
      sqlBlock(factory.connection)
    } catch {
      case e =>
        rollback(factory.connection)
        throw errorHandler.prepare(e)
    }
  }

  def query[T](sql: String, params: Any*)(resultProcessor: ResultSet => T): List[T] = {
    execute { connection =>
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

      val results = statement.executeQuery;

      var processed = List[T]()
      while (results.next()) {
        processed = resultProcessor(results) :: processed
      }
      processed
    }
  }

  def rollback(connection: Connection) {
    if (!connection.getAutoCommit) connection.rollback
  }
}

class DefaultErrorHandler extends SQLHelper.ErrorHandler {
  override def prepare(e: Throwable) = {
    new RuntimeException(e)
  }
}

object SQLHelper {
  trait ConnectionFactory {
    def connection: Connection
  }
  trait ErrorHandler {
    def prepare(e: Throwable): Throwable
  }
}

