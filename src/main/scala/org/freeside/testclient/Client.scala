/**
 *
 */
package org.freeside.easyjdbc
import java.sql.DriverManager

/**
 * @author kjozsa
 */
object Boot {
  Class.forName("org.h2.Driver");
  EasyJDBC.connection = () => DriverManager.getConnection("jdbc:h2:testdb")
  EasyJDBC.errorHandler = { e =>
    e.printStackTrace
    e
  }
}

object Client extends App with EasyJDBC {
  Boot

  sqlUpdate("drop table if exists person; create table person (name varchar(50), city varchar(50), zip char(10), born date, divorced boolean, is_client boolean)")

  // plain query, ok
  sqlQuery("select 1 from dual") { results =>
    println(results.getString(1))
  }

  // simple select
  withConnection { c =>
    val ones = sqlQuery("select 1 from dual") { _.getString(1) }
    println("one type is: " + ones.getClass)
    ones.foreach(s => println("one is: " + s))
  }

  // select for one 
  val client = sqlQueryOne("select is_client from person where name = ?", "Joe") { _.getBoolean(1) }
  assert(client == None)

  val city, zip = sqlQueryOne("select city, zip from person where name = ?", "Joe") { rs =>
    (rs.getString(2), rs.getBoolean(3))
  }

  // with parameters
  val (name, date) = ("Joe", new java.util.Date())
  sqlQuery("select * from person where name = ? and born > ?", name, date) { _.getString(1) }

  // update
  sqlUpdate("update person set divorced = ?", true)
}