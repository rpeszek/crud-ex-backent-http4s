package crudex.persist

import crudex.model.{ElmConfig, ElmLoggerConfig}
import crudex.persist.Common.DefaultPersistEff

import scalaz._
import Scalaz._

/**
  * Temp because it was not STM-ed yet and because everything is untyped
  */
object ElmConfigTemp {
  val tempElmConfig : ElmConfig = ElmConfig("App.Main", ElmLoggerConfig("Std", List("LApp", "LOut", "LMsg")), "")

  def getElmConfig: DefaultPersistEff[ElmConfig] =
    tempElmConfig.pure[DefaultPersistEff]
}
