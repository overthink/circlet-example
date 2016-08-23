package com.markfeeney.circletexample

import com.markfeeney.circlet.Circlet.handler
import com.markfeeney.circlet.middleware.{Cookie, Cookies, Head, MultipartParams, Params}
import com.markfeeney.circlet.{Handler, JettyAdapter, JettyOptions, JettyWebSocket, Middleware, Response}
import org.joda.time.Duration

import scala.util.Random

object Main {

  // I guess I really should add static file serving middleware...
  private val pigLatinHandler = handler { req =>
    val page =
      """
        |<!DOCTYPE html>
        |<html lang="en">
        |<head>
        |  <meta charset="utf-8">
        |  <title>pig latin websocket example</title>
        |</head>
        |<body>
        |  <h1>pig latin websocket example</h1>
        |  <input type="text" id="in" placeholder="enter text and press enter">
        |  <script>
        |    var ws = new WebSocket('ws://' + window.location.host + "/echo-pig-latin/");
        |    ws.onmessage = e => {
        |      if (e.data.length > 0) {
        |        var div = document.createElement('div');
        |        div.innerHTML = e.data;
        |        document.querySelector('body').appendChild(div);
        |      }
        |    }
        |    var input = document.getElementById('in');
        |    input.onkeypress = e => {
        |      if (!e) e = window.event;
        |      var keyCode = e.keyCode || e.which;
        |      if (keyCode === 13) {
        |        ws.send(input.value);
        |        input.value = '';
        |      }
        |    }
        |  </script>
        |</body>
        |</html>
        |
      """.stripMargin
    Response(body = page)
  }

  // very naive
  private def pigLatin(msg: String): String = {
    msg
      .split(' ')
      .filter(_.nonEmpty)
      .map(s => s.substring(1) + s.charAt(0) + "ay")
      .mkString(" ")
  }

  def main(args: Array[String]): Unit = {

    // only Cookies is really used, the rest are just examples of stacking middleware
    val mw: Middleware = Head.mw
      .andThen(Params.mw())
      .andThen(MultipartParams.mw())
      .andThen(Cookies.mw())

    val cookieExample: Handler = handler { req =>
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

    // For nicer routing, check out Usher: https://github.com/overthink/usher
    val routes: Handler = req => req.uri match {
      case "/pig-latin" => pigLatinHandler(req)
      case "/cookies" => mw(cookieExample)(req)
      case _ =>
        respond => respond(Response(body = "Try /cookies or /pig-latin"))
    }

    val pigLatinWs = JettyWebSocket(
      onText = (session, msg) => session.getRemote.sendString(pigLatin(msg))
    )

    val opts = JettyOptions(
      webSockets = Map("/echo-pig-latin/" -> pigLatinWs),
      httpPort = 8888,
      configFn = _.setStopAtShutdown(true),
      join = true)
    JettyAdapter.run(routes, opts)
    println("bye!")
  }

}
