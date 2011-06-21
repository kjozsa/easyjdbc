/**
 *
 */
package org.freeside.jdbchelper

import org.mockito.Mockito._
import org.freeside.jdbchelper.JDBCHelper.ConnectionManager
import org.scalatest.mock.MockitoSugar
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter

/**
 * @author kjozsa
 */
class TestJDBCHelper extends FunSuite with MockitoSugar with BeforeAndAfter {
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
      sqlExecute(c1 => {
        sqlExecute(c2 => {
          assert(c1 eq c2)
        })
      })
    }
  }
}
