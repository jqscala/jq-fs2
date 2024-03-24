package jqfs2

import jq._
import fs2._
import io.circe.{JsonObject, Json}
import io.circe.syntax._

type Filter[F[_]] = 
    Pipe[F, io.circe.Json, io.circe.Json | jq.TypeError]

given [F[_]]: Jq[Filter[F]] with 
    def id: Filter[F] = 
        identity

    def str(s: String): Filter[F] = 
        _ map: _ => 
            Json.fromString(s)

    def error(msg: String): Filter[F] = 
        _ map: _ => 
            TypeError.Custom(msg)

    def iterator: Filter[F] = 
        _ flatMap:
            case IsObject(v) => Stream(v.values.toSeq*)
            case IsArray(v) => Stream(v*)
            case j => Stream(TypeError.CannotIterateOver(j))
   
    def array(f: Filter[F]): Filter[F] =
        _ flatMap: json => 
            f(Stream(json))
                .fold(List[Json|TypeError]())(_ :+ _)
                .flatMap: content => 
                    content.lastOption match
                        case Some(error: TypeError) => Stream(error)
                        case _ => Stream(Json.arr(content.collect{ case j: Json => j }*))

    extension (f1: Filter[F])
        def |(f2: Filter[F]): Filter[F] = 
            _ flatMap: v => 
                f1(Stream(v)) flatMap:
                    case e: TypeError => Stream(e)
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

        private def indexArray(idx: Int): Filter[F] =
            _.flatMap: v1 => 
                f1(Stream(v1)).flatMap:
                    case e: TypeError => Stream(e)
                    case IsArray(vec) => Stream(vec.lift(idx).getOrElse(Json.Null))
                    case v2: Json => Stream(TypeError.CannotIndex(v2, idx.asJson)) 
                        
        private def indexObj(key: String): Filter[F] =
            _.flatMap: v1 => 
                f1(Stream(v1)).flatMap:
                    case e: TypeError => Stream(e)
                    case IsObject(obj) => Stream(obj(key).getOrElse(Json.Null))
                    case v2: Json => Stream(TypeError.CannotIndex(v2, key.asJson))     

        def index(keyF: Filter[F]): Filter[F] = 
            _.flatMap: v => 
                keyF(Stream(v)) flatMap:
                    case e: TypeError => Stream(e)
                    case IsInt(idx) => indexArray(idx)(Stream(v))
                    case IsString(str) => indexObj(str)(Stream(v))
                    case k: Json => Stream(TypeError.CannotIndex(what = v, _with = k))

        def `catch`(f2: Filter[F]): Filter[F] = 
            _ flatMap: j => 
                f1(Stream(j)) flatMap:
                    case j: Json => Stream(j)
                    case e: TypeError => 
                        f2(Stream(Json.fromString(e.toString)))
