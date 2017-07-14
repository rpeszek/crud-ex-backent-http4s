package crudex.view

import org.http4s.circe._
import io.circe._
import io.circe.syntax._
import crudex.model.ElmConfig
import crudex.view.Misc.ScalatagsHtmlEncoder

import scalatags.Text.all._

/**
  */
object ElmApp {
  import io.circe.generic.auto._

  def elmPage: ElmConfig => ConcreteHtmlTag[String] = elmConf =>
       html(
         head(
           scalatags.Text.tags2.title("Elm Crud Example"),
           link(rel:= "stylesheet", `type`:="text/css", href:="static/css/pure.css"),
           link(rel:= "stylesheet", `type`:="text/css", href:="static/css/styles.css")
         ),
         body(
           div(id:="elm-div"),
           script(src:="static/js/elm-app.js"),
           script(toElmScript(elmConf))
         )
       )

  private def toElmScript:  ElmConfig => String = elmConf => {
      s"""
        |var conf = ${elmConf.asJson}
        |var node = document.getElementById('elm-div');
        |var app = Elm.${elmConf.elmProgName}.embed(node, conf);
      """.
        stripMargin
   }

   object implicits {
      implicit val elmPageAsHtml: ScalatagsHtmlEncoder[ElmConfig] = new ScalatagsHtmlEncoder[ElmConfig]{
        def toScalatagsHtml(a:ElmConfig): ConcreteHtmlTag[String] = elmPage(a)
      }
   }
}
