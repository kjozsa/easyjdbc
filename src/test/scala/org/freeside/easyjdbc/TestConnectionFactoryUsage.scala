package org.freeside.easyjdbc

import java.sql.Connection
import org.mockito.Mockito._
import org.scalatest.{ BeforeAndAfter, FunSuite }
import org.scalatest.mock.MockitoSugar

/**
 * @author kjozsa
 */
class TestConnectionFactoryUsage extends FunSuite with MockitoSugar with BeforeAndAfter {

  // counts how many time the connection was borrowed from the factory
  var count: Int = 
  _

  before ({
    count = 0
    EasyJDBC.connection = () => {
      count += 1
      mock[Connection]
    }
  })

  test("subsequent calls use the same connection") ({
    new EasyJDBC {
      sqlExecute(c => {})
      sqlExecute(c => {})
    }
    assert(count === 1)
  })

  test("separate calls use different connection from factory") ({
    new Object with EasyJDBC {
      sqlExecute(c => {})
    }
    assert(count === 1)

    new Object with EasyJDBC {
      sqlExecute(c => {})
    }
    assert(count === 2)
  })

}
