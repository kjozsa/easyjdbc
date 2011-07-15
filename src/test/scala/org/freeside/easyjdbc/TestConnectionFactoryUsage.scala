package org.freeside.easyjdbc

import java.sql.Connection
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.{ BeforeAndAfter, FunSuite }
import org.scalatest.mock.MockitoSugar
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.sql.PreparedStatement

/**
 * @author kjozsa
 */
@RunWith(classOf[JUnitRunner])
class TestConnectionFactoryUsage extends FunSuite with MockitoSugar with BeforeAndAfter {

  // counts how many time the connection was borrowed from the factory
  var count: Int = _

  before({
    count = 0

    EasyJDBC.connectionFactory = { () =>
      count += 1
      val connection = mock[Connection]
      val statement = mock[PreparedStatement]
      when(connection.prepareStatement(any())).thenReturn(statement)
      connection
    }
  })

  test("subsequent calls use the same connection from factory")({
    new EasyJDBC {
      withConnection(c => {
        sqlQuery("select 1 from dual") { rs => }
        sqlQuery("select 1 from dual") { rs => }
      })
    }
    assert(count === 1)
  })

  test("separate calls use different connection from factory")({
    var c1, c2: Connection = null
    new EasyJDBC {
      assert(count === 0)
      withConnection(c => { c1 = c })

      assert(count === 1)
      withConnection(c => { c2 = c })

      assert(count === 2)
      assert(c1 != c2)
    }
  })

  test("separate objects use different connection from factory")({
    new Object with EasyJDBC {
      withConnection(c => {})
    }
    assert(count === 1)

    new Object with EasyJDBC {
      withConnection(c => {})
    }
    assert(count === 2)
  })

}
