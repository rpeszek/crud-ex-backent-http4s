package crudex.view

import scalatags.Text.all.ConcreteHtmlTag

/**
  */
object Misc {

  //TODO convert to using http4s EntityEncoder
  //implement EntityEncoder[ConcreteHtmlTag[String]]
  // or EntityEncoder[Fragment]
  //
  /*
   Typeclass defining HTML view constraint
   */
  trait ScalatagsHtmlEncoder[A] {
     def toScalatagsHtml(a:A): ConcreteHtmlTag[String]
     def asHtmlPage(a:A): String = {
       val res= toScalatagsHtml(a).render
       fixScalatagsQuoting(res)
     }
  }

  object ScalatagsHtmlEncoder {
     def apply[A](implicit ev: ScalatagsHtmlEncoder[A]): ScalatagsHtmlEncoder[A] = ev

    object implicits {
      implicit class ScalatagsHtmlEncoderOps[A](x: A)(implicit o: ScalatagsHtmlEncoder[A]) {
        def asHtmlPage: String = o.asHtmlPage(x)
      }
    }
  }

  private def fixScalatagsQuoting: String => String = stringWithQuotes => {
    val findQuote = "\\&quot;".r
    findQuote.replaceAllIn(stringWithQuotes, "\"")
  }
}
