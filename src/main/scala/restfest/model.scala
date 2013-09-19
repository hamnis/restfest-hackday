package restfest

import java.net.URI
import org.json4s._
import workorder._

case class Sound(uri: URI, volume: Int)

trait Implicits {
  implicit object SoundInputConverter extends InputConverter[Sound] {
    def convert(json: JValue) = {
      val JString(href) = json \ "uri"
      val JInt(volume) = json \ "volume"
      Sound(URI.create(href), volume.toInt)
    }
  }

}

object Implicits extends Implicits
