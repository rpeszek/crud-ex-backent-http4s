package crudex.web

import org.http4s._, org.http4s.dsl._

/**
  *  http://localhost:8080/api/hello/robert
  */
object Sample {
  val helloWorldService = HttpService {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name.")
  }
}
