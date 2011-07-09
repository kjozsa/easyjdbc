/**
 *
 */
package org.freeside.easyjdbc

import java.sql.Connection
import org.freeside.easyjdbc.EasyJDBC.ConnectionManager
import org.junit.runner.RunWith
import org.mockito.Matchers.any
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

/**
 * @author kjozsa
 */
@RunWith(classOf[JUnitRunner])
class TestConnectionManagement extends FunSuite with MockitoSugar with BeforeAndAfter {
  val connectionManager = mock[ConnectionManager]

  before {
    EasyJDBC.thread.set(connectionManager)
  }

  test("sqlExecute borrows threadlocal connection") {
    new Object with EasyJDBC {
      sqlExecute(c => {})
    }

    verify(connectionManager).borrow
    verify(connectionManager).back
  }

  test("nested sqlExecute uses the same connection") {
    new Object with EasyJDBC {
      sqlExecute { c1 =>
        sqlExecute { c2 =>
          assert(c1.eq(c2))
        }
      }
    }
  }

  test("connection is rolled back on error") {
    val connection = mock[Connection]
    when(connectionManager.borrow).thenReturn(connection)

    intercept[RuntimeException] {
      new Object with EasyJDBC {
        sqlExecute { c => throw new RuntimeException }
      }
    }
    verify(connection).rollback
  }

  test("errorHandler is called on error") {
    val errorHandler = mock[Throwable => Throwable]
    EasyJDBC.errorHandler = errorHandler

    intercept[RuntimeException] {
      new Object with EasyJDBC {
        sqlExecute { c => throw new RuntimeException }
      }
    }
  }
}
