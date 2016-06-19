import com.markfeeney.circlet._

object Main {

  def main(args: Array[String]): Unit = {
    val app = Circlet.handler { _ => Response(body = "Hello friends\n") }
    JettyAdapter.run(app, JettyOptions(httpPort = 8888))
  }

}
