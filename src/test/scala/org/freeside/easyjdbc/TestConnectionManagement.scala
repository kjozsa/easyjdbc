package org.freeside.easyjdbc

import java.sql.Connection
import org.mockito.Mockito._
import org.scalatest.{ BeforeAndAfter, FunSuite }
import org.scalatest.mock.MockitoSugar
import org.freeside.easyjdbc.EasyJDBC.ConnectionFactory

/**
 * @author kjozsa
 */

class TestConnectionManagement extends FunSuite with MockitoSugar with BeforeAndAfter {
  val factory: ConnectionFactory = mock[ConnectionFactory]

  var count = 0

  before {
    EasyJDBC.factory = new ConnectionFactory {
      def connection = {
        count += 1
        println("count: " + count)
        mock[Connection]
      }
    }
  }

  test("subsequent calls use different connection") {
    new Object with EasyJDBC {
      sqlExecute(c => {})
    }
    new Object with EasyJDBC {
      sqlExecute(c => {})
    }
  }

}
