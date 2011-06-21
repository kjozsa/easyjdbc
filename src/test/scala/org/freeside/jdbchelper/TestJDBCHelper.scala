/**
 *
 */
package org.freeside.jdbchelper

import org.mockito.Mockito._
import org.freeside.jdbchelper.JDBCHelper.ConnectionManager
import org.scalatest.mock.MockitoSugar
import org.scalatest.FunSuite

/**
 * @author kjozsa
 */
class TestJDBCHelper extends FunSuite with MockitoSugar {
  test("not yet") { pending }

  test("hello world") {
    assert(1 + 1 == 2)
  }

  test("sqlExecute handles threadlocal") {
    val connectionManager = mock[ConnectionManager]
    JDBCHelper.thread.set(connectionManager)

    new Object with JDBCHelper {
      sqlExecute(c => {})
      println("boo")
    }

    verify(connectionManager).connection
    verify(connectionManager).back
  }
}
