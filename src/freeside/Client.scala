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

  // wtf1
  sqlhelper.query("select 1 from dual") { _.getString(1) }

  val name, date = ("Joe", new java.util.Date())
  //  val rs = sqlhelper.query3("select * from person where name = ? and born > ?") << (name, date)

  sqlhelper.query("select * from person where name = ? and born > ?", name, date) { _.getString(1) }

}