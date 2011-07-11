package org.freeside.easyjdbc

import java.sql.Connection
import org.mockito.Mockito._
import org.scalatest.{ BeforeAndAfter, FunSuite }
import org.scalatest.mock.MockitoSugar
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

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
      mock[Connection]
    }
  })

  test("subsequent calls use the same connection")({
    new EasyJDBC {
      withConnection(c => {})
      withConnection(c => {})
    }
    assert(count === 1)
  })

  test("separate calls use different connection from factory")({
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
