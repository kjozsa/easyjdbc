/**
 *
 */
package org.freeside.easyjdbc
import org.freeside.easyjdbc.EasyJDBC.ErrorHandler
import org.freeside.easyjdbc.EasyJDBC.JNDIConnectionFactory

/**
 * @author kjozsa
 */
class Boot {
  EasyJDBC.factory = new JNDIConnectionFactory("java:comp/env/jdbc/jc_gateway")
  EasyJDBC.errorHandler = new ErrorHandler {
    override def handle(e: Throwable) = {
      println("oops")
      e
    }
  }
}

class Parent

class Client extends Parent with EasyJDBC {
  // plain query, ok
  sqlQuery("select 1 from dual") { results =>
    println(results.getString(1))
  }

  // simple select
  sqlQuery("select 1 from dual") { _.getString(1) }

  // select for one 
  val client = sqlQueryOne("select is_client from person where name = ?", "Joe") { _.getBoolean(1) }
  val city, zip = sqlQueryOne("select city, zip from person where name = ?", "Joe") { rs => (rs.getString(2), rs.getBoolean(3)) }

  // with parameters
  val name, date = ("Joe", new java.util.Date())
  sqlQuery("select * from person where name = ? and born > ?", name, date) { _.getString(1) }

  // update
  sqlUpdate("update person set divorced = ?", true)
}