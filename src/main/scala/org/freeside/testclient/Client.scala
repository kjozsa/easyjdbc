/**
 *
 */
package org.freeside.easyjdbc
import java.sql.DriverManager
import java.io.File
import java.sql.ResultSet

/**
 * @author kjozsa
 */
trait ConfiguredEasyJDBC extends EasyJDBC {
  Class.forName("org.h2.Driver");
  val dbLocation = System.getProperty("java.io.tmpdir") + "/testdb"

  val connectionFactory = () => DriverManager.getConnection("jdbc:h2:" + dbLocation)
}

object Client extends App with ConfiguredEasyJDBC {
  sqlUpdate("""
		  drop table if exists person; 
		  create table person (name varchar(50), city varchar(50), zip int, born date, divorced boolean, is_client boolean);
		  insert into person (name, city, zip) values ('Bill', 'Budapest', 1);
		  insert into person (name, city, zip) values ('Susan', 'London', 23456);
		  """)

  // fetch none
  val client = sqlFetchOne("select is_client from person where name = ?", "Joe") { _.getBoolean(1) }
  assert(client == None)

  // fetch some
  val personList = sqlFetch("select name from person") { _.getString(1) }
  println("fetched: " + personList.size)
  println(personList.head)

  // fetch one
  println("count person: " + sqlFetchOne("select count(*) from person") { _.getInt(1) })

  // select Option[Tuple]
  val results = sqlFetchOne("select city, zip from person where name = ?", "Susan") { rs =>
    (rs.getString(1), rs.getInt(2))
  }
  val (city, zip) = results.get
  println("city: " + city + ", zip: " + zip)

  // alternate syntax with auto count
  val (city2, zip2) = sqlFetchOne("select city, zip from person where name = ?", "Susan") { rs =>
    (rs.nextString, rs.nextInt)
  }.get

  // select with parameters
  val (name, date) = ("Joe", new java.util.Date())
  sqlQuery("select * from person where name = ? and born > ?", name, date) { _.getString(1) }

  // execute update
  println("updated: " + sqlUpdate("update person set divorced = ?", true))
}