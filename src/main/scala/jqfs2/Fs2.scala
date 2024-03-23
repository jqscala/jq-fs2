package jqfs2

import jq._
import fs2._
import io.circe.{JsonObject, Json}

object Fs2:

    type Filter[F[_]] = 
        Pipe[F, io.circe.Json, io.circe.Json | Error]

    given [F[_]]: Jq[Filter[F]] with 

        object IsArray: 
            def unapply(v: Json): Option[Vector[Json]] = 
                v.asArray

        object IsObject: 
            def unapply(v: Json): Option[JsonObject] = 
                v.asObject

        object IsString: 
            def unapply(v: Json): Option[String] = 
                v.asString

        def id: Filter[F] = 
            identity

        def str(s: String): Filter[F] = 
            _ map: _ => 
                Json.fromString(s)

        def error(msg: String): Filter[F] = 
            _ map: _ => 
                Error.Custom(msg)

        def iterator: Filter[F] = 
            _ flatMap:
                case IsArray(v) => Stream(v*)
                case j => Stream(Error.CannotIterateOver(j))

        extension (f1: Filter[F])
            def |(f2: Filter[F]): Filter[F] = 
                _ flatMap: v => 
                    f1(Stream(v)) flatMap:
                        case e: Error => Stream(e)
                        case j: Json => 
                            f2(Stream(j)).takeThrough: 
                                case j: Json => true
                                case _ => false
                    

            def concat(f2: Filter[F]): Filter[F] = 
                _.flatMap: json => 
                    (f1(Stream(json)) ++ f2(Stream(json)))
                        .takeThrough: 
                            case j: Json => true
                            case _ => false

            private def indexObj(key: String)(v: Json): Stream[F, Json | Error] = 
                f1(Stream(v)) map: 
                    case IsObject(obj) => obj(key).getOrElse(Json.Null)
                    case j: Json => Error.CannotIndex(j)
                    case error => error

            def index(f2: Filter[F]): Filter[F] = 
                _ flatMap: v => 
                    f2(Stream(v)) flatMap:
                        case IsString(s) => indexObj(s)(v)
                        case k: Json => Stream(Error.CannotIndexObjectWith(k))
                        case error => Stream(error)

            def `catch`(f2: Filter[F]): Filter[F] = 
                _ flatMap: j => 
                    f1(Stream(j)) flatMap:
                        case j: Json => Stream(j)
                        case e: Error => 
                            f2(Stream(Json.fromString(e.toString)))

        def array(f: Filter[F]): Filter[F] =
            _ flatMap: json => 
                f(Stream(json))
                    .fold(List[Json|Error]())(_ :+ _)
                    .flatMap: content => 
                        content.lastOption match
                            case Some(error: Error) => Stream(error)
                            case _ => Stream(Json.arr(content.collect{ case j: Json => j }*))
