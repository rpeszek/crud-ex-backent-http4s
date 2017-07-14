package crudex.utils

import org.http4s._
import org.http4s.MediaType
import org.http4s.MediaType.`text/html`
import org.http4s.headers.`Content-Type`

import scalatags.Text.all._

/**
  */
object ScalatagsInstances {
  type Html = ConcreteHtmlTag[String]

  def htmlScalatagEncoder(implicit charset: Charset = DefaultCharset): EntityEncoder[Html] =
    contentEncoder(`text/html`)

  private def contentEncoder(mediaType: MediaType)(implicit charset: Charset = DefaultCharset): EntityEncoder[Html] =
    EntityEncoder.stringEncoder(charset).contramap[Html](content => fixScalatagsQuoting(content.render))
      .withContentType(`Content-Type`(mediaType, charset))

  private def fixScalatagsQuoting: String => String = stringWithQuotes => {
    val findQuote = "\\&quot;".r
    findQuote.replaceAllIn(stringWithQuotes, "\"")
  }
}
