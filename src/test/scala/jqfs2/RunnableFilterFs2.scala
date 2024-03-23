package jqfs2

import io.circe.Json
import cats.effect.unsafe.implicits.global
import fs2.Stream
import cats.effect.IO

import jq._

given Fs2Run: RunnableFilter[Fs2.Filter[IO]] with

    extension [A](st: Stream[IO, A])
        def run: List[A] = 
            st.compile.toList.unsafeRunSync()
            
    extension (input: List[Json])
        def through(r: Fs2.Filter[IO]): List[Json | Error] = 
            r(Stream[IO, Json](input*))
                .run

