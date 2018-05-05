package org.bykn.fastparse_cats

import scodec.bits.ByteVector

object ByteInstances extends FastParseCatsGeneric[Byte, ByteVector] {
  val api = fastparse.byte.all
}
