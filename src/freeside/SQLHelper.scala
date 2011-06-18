/**
 *
 */
package freeside

import java.sql.Connection
import java.sql.ResultSet
import java.sql.PreparedStatement

/**
 * @author kjozsa
 */
class SQLHelper(
  implicit factory: SQLHelper.ConnectionFactory,
  errorHandler: SQLHelper.ErrorHandler = new DefaultErrorHandler) {

  println("using factory: " + factory + ", errorhandler: " + errorHandler)

  def execute[T](sqlBlock: Connection => T): T = {
    try {
      sqlBlock(factory.connection)
    } catch {
      case e =>
        rollback(factory.connection)
        throw errorHandler.prepare(e)
    }
  }

  def query[T](sql: String)(resultProcessor: ResultSet => T): List[T] = {
    execute { connection =>
      val statement = connection.prepareStatement(sql);
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
