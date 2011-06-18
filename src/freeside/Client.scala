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

  sqlhelper.execute(connection => {
    "boo"
  })

  sqlhelper.query("select 1 from dual") { results: =>
    println(results.getString(1))
  }

//  sqlhelper.query("select * from lofasz where id = ?") { statement =>
//    statement.setString(1, "lofasz")
//  } { results =>
//    println(results.getString(1))
//  }
}