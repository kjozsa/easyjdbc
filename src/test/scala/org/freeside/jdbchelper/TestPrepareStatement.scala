/**
 *
 */
package org.freeside.jdbchelper

import org.mockito.Mockito._
import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar
import org.scalatest.BeforeAndAfter
import java.sql.Connection
import org.mockito.Matchers._
import java.sql.PreparedStatement
import java.sql.Types

/**
 * @author kjozsa
 */
class TestPrepareStatement extends FunSuite with MockitoSugar with BeforeAndAfter {
  val connection = mock[Connection]
  val statement = mock[PreparedStatement]

  before {
    reset(connection)
    reset(statement)
    when(connection.prepareStatement(any())).thenReturn(statement)
  }

  test("handle wrong number of parameters") {
    intercept[AssertionError] {
      new Object with JDBCHelper {
        prepareStatement(connection, "select missing from parameter where stuff = ?")
      }
    }
  }

  test("parameterless") {
    new Object with JDBCHelper {
      prepareStatement(connection, "select 1 from dual")
    }
  }

  test("null parameters") {
    new Object with JDBCHelper {
      prepareStatement(connection, "select blah ? and ?", null, None)
    }
    verify(statement).setNull(1, Types.NULL)
    verify(statement).setNull(2, Types.NULL)
  }

  test("Option type parameter") {
    new Object with JDBCHelper {
      prepareStatement(connection, "..where name = ?", Some("Joe"))
    }
    verify(statement).setString(1, "Joe")
  }

  test("string parameter") {
    new Object with JDBCHelper {
      prepareStatement(connection, "select * from person where name = ?", "Joe")
    }
    verify(statement).setString(1, "Joe")
  }

  test("boolean parameter") {
    new Object with JDBCHelper {
      prepareStatement(connection, "select * from person where divorced = ?", false)
    }
    verify(statement).setBoolean(1, false)
  }
}