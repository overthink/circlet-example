package com.markfeeney.circletexample

import com.markfeeney.circlet.middleware._
import com.markfeeney.circlet.{Circlet, JettyAdapter, JettyOptions, Response, _}
import org.joda.time.Duration

import scala.util.Random

object Main {

  def main(args: Array[String]): Unit = {

    val mw: Middleware = Head.mw
      .andThen(Params.mw())
      .andThen(MultipartParams.mw())
      .andThen(Cookies.mw())

    val handler: Handler = Circlet.handler { req =>
      Cookies.get(req, "id") match {
        case None =>
          val id = Random.nextInt(1000000)
          val body = s"No id yet, going to set $id (5 second ttl)"
          val c = Cookie(value = id.toString, maxAge = Some(new Duration(5000)))
          Cookies.add(Response(body = body), "id", c)
        case Some(id) =>
          Response(body = s"Id is $id")
      }
    }

    val app = mw(handler)
    val opts = JettyOptions(httpPort = 8888, configFn = _.setStopAtShutdown(true), join = true)
    JettyAdapter.run(app, opts)
    println("bye!")
  }

}
