package org.freeside.easyjdbc

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import org.junit.runner.RunWith
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

/**
 * @author kjozsa
 */
@RunWith(classOf[JUnitRunner])
class TestExecution extends FunSuite with MockitoSugar with BeforeAndAfter {

  test("plain execute") {
    val connection = mock[Connection]
    EasyJDBC.connectionFactory = () => connection

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