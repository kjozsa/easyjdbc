/**
 *
 */
package org.freeside.easyjdbc
import java.sql.Connection
import javax.naming.InitialContext
import javax.sql.DataSource

/**
 * @author kjozsa
 */
class JNDIConnectionFactory(jndiName: String) {
  val dataSource: DataSource = new InitialContext().lookup(jndiName).asInstanceOf[DataSource]

  def connection: Connection = {
    dataSource.getConnection
  }

  override def toString = "JNDI connection factory to " + jndiName
}