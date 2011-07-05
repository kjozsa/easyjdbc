/**
 *
 */
package org.freeside.easyjdbc

import java.sql.Connection
import org.freeside.easyjdbc.EasyJDBC.{ ConnectionManager, ErrorHandler }
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{ BeforeAndAfter, FunSuite }

/**
 * @author kjozsa
 */
class TestMockedConnectionManagement extends FunSuite with MockitoSugar with BeforeAndAfter {
  val connectionManager = mock[ConnectionManager]

  before {
    EasyJDBC.thread.set(connectionManager)
  }

  test("sqlExecute borrows threadlocal connection") {
    new Object with EasyJDBC {
      sqlExecute(c => {})
      println("boo")
    }

    verify(connectionManager).connection
    verify(connectionManager).back
  }

  test("nested sqlExecute uses the same connection") {
    new Object with EasyJDBC {
      sqlExecute { c1 =>
        sqlExecute { c2 =>
          assert(c1 === c2)
        }
      }
    }
  }

  test("connection is rolled back on error") {
    val connection = mock[Connection]
    when(connectionManager.connection).thenReturn(connection)

    intercept[RuntimeException] {
      new Object with EasyJDBC {
        sqlExecute { c => throw new RuntimeException }
      }
    }
    verify(connection).rollback
  }

  test("errorHandler is called on error") {
    val errorHandler = mock[ErrorHandler]
    EasyJDBC.errorHandler = errorHandler

    intercept[RuntimeException] {
      new Object with EasyJDBC {
        sqlExecute { c => throw new RuntimeException }
      }
    }
    verify(errorHandler).handle(any())
  }
}
