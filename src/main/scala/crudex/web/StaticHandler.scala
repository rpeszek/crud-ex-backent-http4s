package crudex.web

import org.http4s._
import org.http4s.dsl._
import java.io.File
import scalaz.concurrent.Task


/**
  */
object StaticHandler {
  val staticService = HttpService {
    case request @ GET -> Root / "static" / dir / file => {
      //println(System.getProperty("user.dir"))
      StaticFile.fromFile(new File(s"static/$dir/$file"), Some(request))
        .map(Task.now) // This one is require to make the types match up
        .getOrElse(NotFound()) // In case the file doesn't exist
    }
  }
}
