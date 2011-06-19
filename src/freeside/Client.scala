/**
 *
 */
package freeside
import java.sql.ResultSet

/**
 * @author kjozsa
 */
object Client extends App {
  object World {
    implicit val connectionFactory = new JNDIConnectionFactory("jdbc/jc_gateway")
    //    implicit val errorHandler = new SQLHelper.ErrorHandler {
    //      override def handle(e: Throwable) {
    //
    //      }
    //    }
    implicit val sqlhelper = new SQLHelper
  }
  import World._

  // plain usage, ok
  sqlhelper.execute(connection => {
    "boo"
  })

  // plain query, ok
  sqlhelper.query("select 1 from dual") { results =>
    println(results.getString(1))
  }

  // simple select
  sqlhelper.query("select 1 from dual") { _.getString(1) }

  // select for one 
  val zip: Option[Tuple2[String, Boolean]] = sqlhelper.queryOne("select city, zip from person where name = ?", "Joe") { rs => (rs.getString(2), rs.getBoolean(3)) }
  val client: Option[Boolean] = sqlhelper.queryOne("select is_client from person where name = ?", "Joe") { _.getBoolean(1) }

  // with parameters
  val name, date = ("Joe", new java.util.Date())
  sqlhelper.query("select * from person where name = ? and born > ?", name, date) { _.getString(1) }

  // update
  sqlhelper.update("update person set divorced = ?", true)

}