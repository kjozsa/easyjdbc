/**
 *
 */
package freeside

import freeside.SQLHelper.ConnectionFactory
import java.sql.Connection
import javax.naming.InitialContext
import javax.naming.Context
import javax.sql.DataSource

/**
 * @author kjozsa
 */
class JNDIConnectionFactory(jndiName: String) extends ConnectionFactory {

  override def connection: Connection = {
    val ic = new InitialContext()
    val env = ic.lookup("java:comp/env").asInstanceOf[Context]
    env.lookup(jndiName).asInstanceOf[DataSource].getConnection;
  }

  override def toString = "JNDI connection factory to " + jndiName
}
