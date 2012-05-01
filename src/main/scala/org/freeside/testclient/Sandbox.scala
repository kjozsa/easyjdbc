/**
 *
 */
package org.freeside.testclient
import java.sql.ResultSet
import java.sql.Connection
import org.freeside.easyjdbc.EasyResultSet

/**
 * @author kjozsa
 */
trait SomeTrait {
  val connectionFactory: () => Connection

  implicit def rsToString(rs: ResultSet) = rs.getString(1)
  implicit def rsToInt(rs: ResultSet) = rs.getInt(1)
  implicit def rsToBoolean(rs: ResultSet) = rs.getBoolean(1)

  implicit def rsToTupleStringInt(rs: ResultSet): Tuple2[String, Int] = (rs.getString(1), rs.getInt(2))
}

class Sample {
  val rs: EasyResultSet = null

  //  val z: String = rs // rs.nextString

}

trait ConfiguredSome extends SomeTrait {
  val connectionFactory = null
}

object Sandbox extends ConfiguredSome {
}