/**
 *
 */
package freeside

/**
 * @author kjozsa
 *
 */
object Main extends App {

  trait Something
  trait Something2

  implicit object XSomething extends Something
  implicit object XSomething2 extends Something2

  def x(a: Int)(b: Int)(implicit zz: Something2, z: Something) {}

  def y(a: Int)(b: Int) = { a * b }

  def << = 3

  println("hello")
  println("a" * 5)
  //  x(2)(3)(XSomething2)
  println(y(2)(3))
  val q = y(2)(_)
  println(q)

  case class UserData(username: String, password: String)
}