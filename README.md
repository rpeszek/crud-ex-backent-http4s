Example backend CRUD project in Scala using http4s and (currently ScalaSTM).
See my CRUD umbrella project:  [typesafe-test.web-polyglot](https://github.com/rpeszek/typesafe-test.web-polyglot.git).

I am new to Scala.  This project allowed me (so far) to play with

* http4s library (I found it to be quite nice!)
* Scalatags (just a bit because currently the app is single page load and client is Elm)
* ScalaSTM (converted small subset of it to Haskell-like monadic API, consistency is good!, why one more ad-hoc API?)
* doobie sql library over H2DB
* Polymorphic Http endpoints and polymorphic persistence effects using type class pattern

