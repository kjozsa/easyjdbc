package org.freeside.easyjdbc

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{ BeforeAndAfter, FunSuite }
import org.freeside.easyjdbc.EasyJDBC.{ DefaultErrorHandler, ConnectionFactory }
import java.sql.{ ResultSet, PreparedStatement, Connection }

/**
 * @author kjozsa
 */
class TestExecution extends FunSuite with MockitoSugar with BeforeAndAfter {

  before {
    EasyJDBC.factory = mock[ConnectionFactory]
    EasyJDBC.errorHandler = DefaultErrorHandler

    val connection = mock[Connection]
    when(EasyJDBC.factory.connection).thenReturn(connection)

    val statement = mock[PreparedStatement]
    when(connection.prepareStatement(any())).thenReturn(statement)

    val results = mock[ResultSet]
    when(statement.executeQuery()).thenReturn(results)

  }

  test("plain execute") {
    new Object with EasyJDBC {
      val names = sqlQuery("select * from person") { rs =>
        println("resultset: " + rs)
      }
    }
  }
}