/**
 *
 */
package org.freeside.easyjdbc

import org.mockito.Mockito._
import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar
import org.scalatest.BeforeAndAfter
import java.sql.Connection
import org.mockito.Matchers._
import java.sql.PreparedStatement
import java.sql.Types
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.BeforeAndAfterEach

/**
 * @author kjozsa
 */
@RunWith(classOf[JUnitRunner])
class TestPrepareStatement extends FunSuite with MockitoSugar with BeforeAndAfterEach {
  val connection = mock[Connection]
  val statement = mock[PreparedStatement]

  override def beforeEach {
    reset(connection)
    reset(statement)
    when(connection.prepareStatement(any())).thenReturn(statement)
  }

  test("handle wrong number of parameters") {
    intercept[AssertionError] {
      new Object with EasyJDBC {
        createStatement(connection, "select missing from parameter where stuff = ?")
      }
    }

    intercept[AssertionError] {
      new Object with EasyJDBC {
        createStatement(connection, "select missing from parameter where stuff = ?", "too", "much")
      }
    }
  }

  test("parameterless") {
    new Object with EasyJDBC {
      createStatement(connection, "select 1 from dual")
    }
  }

  test("null parameters") {
    new Object with EasyJDBC {
      createStatement(connection, "select blah ? and ?", null, None)
    }
    verify(statement).setNull(1, Types.NULL)
    verify(statement).setNull(2, Types.NULL)
  }

  test("Option type") {
    new Object with EasyJDBC {
      createStatement(connection, "..where name = ?", Some("Joe"))
    }
    verify(statement).setString(1, "Joe")
  }

  test("string type") {
    new Object with EasyJDBC {
      createStatement(connection, "select * from person where name = ?", "Joe")
    }
    verify(statement).setString(1, "Joe")
  }

  test("boolean type") {
    new Object with EasyJDBC {
      createStatement(connection, "select * from person where divorced = ?", false)
    }
    verify(statement).setBoolean(1, false)
  }

  test("byte type") { pending }
  test("int type") { pending }
  test("long type") { pending }
  test("float type") { pending }
  test("double type") { pending }
  test("timestamp type") { pending }
  test("bigdecimal type") { pending }
}