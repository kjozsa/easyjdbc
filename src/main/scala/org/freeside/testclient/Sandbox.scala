/**
 *
 */
package org.freeside.testclient
import java.sql.ResultSet
import java.sql.Connection

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

trait ConfiguredSome extends SomeTrait {
  val connectionFactory = { null }
}

class Else(c: () => Connection) {

}

object Sandbox extends App with ConfiguredSome {
  val something: Option[Tuple2[String, Int]] = Some("a", 1)

  val (a, one) = something get

  val rs: ResultSet = null

  val tup: Tuple2[String, Int] = rs

  val z = new Else(connectionFactory)
}