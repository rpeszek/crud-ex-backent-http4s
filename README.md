Example backend CRUD project in Scala using http4s and (currently ScalaSTM).
See my CRUD umbrella project:  [typesafe-web-polyglot](https://github.com/rpeszek/typesafe-web-polyglot).

I am new to Scala.  This project uses

* http4s library (I found it to be quite nice!)
* spec2 tests using http4s client testing restful web endpoints
* Scalatags (just a bit because currently the app is single page load and client is Elm)
* ScalaSTM (converted small subset of it to Haskell-like monadic API, consistency is good!, why one more ad-hoc API?)
* ScalaCheck test for included STM monad implementation
* doobie sql library
* spec2 test for SQL schema correctness

My goal was to implement polymorphic Http endpoints and polymorphic persistence effects.

