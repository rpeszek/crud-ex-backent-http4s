Example backend CRUD project in Scala using http4s and (currently ScalaSTM).
See my CRUD umbrella project:  [typesafe-web-polyglot](https://github.com/rpeszek/typesafe-web-polyglot.git).

I am new to Scala.  This project allowed me (so far) to play with

* http4s library (I found it to be quite nice!)
* Scalatags (just a bit because currently the app is single page load and client is Elm)
* ScalaSTM (converted small subset of it to Haskell-like monadic API, consistency is good!, why one more ad-hoc API?)
* Using type class pattern to write polymorphic Http endpoints and polymorphic persistence effects
