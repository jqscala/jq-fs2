package jqfs2

import jq._
import cats.effect.IO

class Fs2Spec extends JqSpec[Fs2.Filter[IO]]