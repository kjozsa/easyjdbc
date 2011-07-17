/**
 *
 */
package org.freeside.easyjdbc
import java.sql.DriverManager
import java.io.File

/**
 * @author kjozsa
 */
object Boot {
  Class.forName("org.h2.Driver");
  val dbLocation = System.getProperty("java.io.tmpdir") + "/testdb"
  EasyJDBC.connectionFactory = () => DriverManager.getConnection("jdbc:h2:" + dbLocation)
  EasyJDBC.errorHandler = { e =>
    e.printStackTrace
    e
  }
}

object Client extends App with EasyJDBC {
  Boot

  sqlUpdate("""
		  drop table if exists person; 
		  create table person (name varchar(50), city varchar(50), zip int, born date, divorced boolean, is_client boolean);
		  insert into person (name, city, zip) values ('Bill', 'Budapest', 1);
		  insert into person (name, city, zip) values ('Susan', 'London', 23456);
		  """)

  // fetch some
  val person = sqlFetch("select name from person") { _.getString(1) }
  println("fetched: " + person.size)
  println(person.head)

  // fetch one
  println("count person: " + sqlFetchOne("select count(*) from person") { _.getInt(1) })

  // fetch none
  val client = sqlFetchOne("select is_client from person where name = ?", "Joe") { _.getBoolean(1) }
  assert(client == None)

  // select tuple
  val results = sqlFetchOne("select city, zip from person where name = ?", "Susan") { rs =>
    (rs.getString(1), rs.getInt(2))
  }

  val (city, zip) = results.get
  println("city: " + city + ", zip: " + zip)

  // select with parameters
  val (name, date) = ("Joe", new java.util.Date())
  sqlQuery("select * from person where name = ? and born > ?", name, date) { _.getString(1) }

  // update
  println("updated: " + sqlUpdate("update person set divorced = ?", true))
}