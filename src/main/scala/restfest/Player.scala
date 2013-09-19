package restfest

import javafx.scene.media._
import java.net.URI

object Player {
  val Type = URI.create("http://mogsie.com/2013/workflow/play-sound")

  def create(href: URI, volume: Int = 100): AudioClip = {
    val builder = AudioClipBuilder.create()    
    builder.source(href.toString)
    builder.volume(volume / 100.0)
    builder.build()
  }

  def create(sound: Sound): AudioClip = create(sound.href, sound.volume)
}
