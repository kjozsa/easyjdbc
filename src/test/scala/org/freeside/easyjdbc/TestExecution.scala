package org.freeside.easyjdbc

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{ BeforeAndAfter, FunSuite }
import java.sql.{ ResultSet, PreparedStatement, Connection }
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * @author kjozsa
 */
class TestExecution extends FunSuite with MockitoSugar with BeforeAndAfter {

  test("plain execute") {
    val connection = mock[Connection]
    EasyJDBC.connection = () => connection

    val statement = mock[PreparedStatement]
    when(connection.prepareStatement(any())).thenReturn(statement)

    val results = mock[ResultSet]
    when(statement.executeQuery()).thenReturn(results)

    new Object with EasyJDBC {
      sqlQuery("select * from person") { rs =>
        println("resultset: " + rs)
      }
    }
  }
}