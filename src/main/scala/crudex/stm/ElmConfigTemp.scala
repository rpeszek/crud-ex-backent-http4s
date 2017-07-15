package crudex.stm

import crudex.model.{ElmConfig, ElmLoggerConfig}
import crudex.utils.Common.Handler

import scalaz._
import Scalaz._
import scalaz.effect.IO

/**
  * Temp because it was not STM-ed yet and because everything is untyped
  */
object ElmConfigTemp {
  val tempElmConfig : ElmConfig = ElmConfig("App.Main", ElmLoggerConfig("Std", List("LApp", "LOut", "LMsg")), "")

  def getElmConfig: Handler[ElmConfig] =
    tempElmConfig.pure[IO]
}
