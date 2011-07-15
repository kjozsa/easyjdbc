/**
 *
 */
package org.freeside.easyjdbc
import java.sql.Connection

/**
 * @author kjozsa
 */
private[easyjdbc] class ConnectionManager(connectionFactory: () => Connection, connectionCleaner: Connection => Unit) {
  private var depth = 0
  private var connection: Connection = _

  require(connectionFactory != null, "No factory configured for EasyJDBC. Make sure you set the EasyJDBC.connectionFactory field!")

  def borrow = {
    if (depth == 0) connection = connectionFactory()
    depth += 1
    connection
  }

  def back {
    depth -= 1
    if (depth == 0) {
      close(connection)
    }
  }

  private def close(connection: Connection) {
    try {
      connectionCleaner(connection)
    } catch {
      case e => // silent
    }
  }
}