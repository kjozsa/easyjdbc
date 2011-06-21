/**
 *
 */
package org.freeside.jdbchelper

import org.mockito.Mockito._
import org.freeside.jdbchelper.JDBCHelper.ConnectionManager
import org.scalatest.mock.MockitoSugar
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import java.sql.Connection
import org.freeside.jdbchelper.JDBCHelper.ErrorHandler
import org.mockito.Mockito
import org.mockito.Matchers

/**
 * @author kjozsa
 */
class TestConnectionManagement extends FunSuite with MockitoSugar with BeforeAndAfter {
  val connectionManager = mock[ConnectionManager]

  before {
    JDBCHelper.thread.set(connectionManager)
  }

  test("sqlExecute borrows threadlocal connection") {
    new Object with JDBCHelper {
      sqlExecute(c => {})
      println("boo")
    }

    verify(connectionManager).connection
    verify(connectionManager).back
  }

  test("nested sqlExecute uses the same connection") {
    new Object with JDBCHelper {
      sqlExecute { c1 =>
        sqlExecute { c2 =>
          assert(c1 eq c2)
        }
      }
    }
  }

  test("connection is rolled back on error") {
    val connection = mock[Connection]
    when(connectionManager.connection).thenReturn(connection)

    intercept[RuntimeException] {
      new Object with JDBCHelper {
        sqlExecute { c => throw new RuntimeException }
      }
    }
    verify(connection).rollback
  }

  test("errorHandler is called on error") {
    val errorHandler = mock[ErrorHandler]
    JDBCHelper.errorHandler = errorHandler

    intercept[RuntimeException] {
      new Object with JDBCHelper {
        sqlExecute { c => throw new RuntimeException }
      }
    }
    verify(errorHandler).handle(Matchers.any())
  }
}
