package restfest

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._
import unfiltered.directives._
import Directives._
import java.io._
import net.hamnaberg.json.collection.{Item, JsonCollection}
import java.net.URI
import java.util.UUID
import scala.util.Random
import workorder.WorkOrder
import org.json4s._
import unfiltered.response.ContentType

object Server extends App {
  unfiltered.jetty.Http(1337).plan(Resources).run()
}

object Resources extends Plan {
  val selectable = {
    val files = new java.io.File("/Users/maedhros/Music/In Flames/Clayman").getAbsoluteFile.listFiles(new FileFilter {
      def accept(pathname: File) = pathname.toString.endsWith(".mp3")
    })
    println(files.toSeq)
    files.map(f => UUID.randomUUID().toString -> f).toMap
  }


  val intent = Directive.Intent[Any, Any] {
    case Path("/queue") => for {
      _ <- GET
    } yield {
      Ok ~> ContentType("application/vnd.collection+json") ~> new ResponseWriter {
        def write(os: OutputStreamWriter) {
          queue.writeTo(os)
        }
      }
    }
    case Path(Seg("work-item" :: item :: Nil)) => for {
      _ <- GET
      _ <- commit
      i <- getOrElse(selectable.get(item), NotFound)
    } yield {
      Ok ~> ContentType("application/vnd.mogsie.work-order+json") ~> {
        val input = JObject("uri" -> JString("/binary/" + item) , "volume" -> JInt(100))
        val wo: JValue = WorkOrder(Player.Type, input, Some(URI.create("/work-item/" + item + "/start")), None, Some(URI.create("/work-item/" + item + "/complete")), None).asJson
        new ResponseWriter {
          def write(os: OutputStreamWriter) {
            os.write(JM.pretty(JM.render(wo)))
          }
        }
      }
    }
    case Path(Seg("work-item" :: item :: "start" :: Nil)) => for {
      _ <- POST
      _ <- commit
      i <- getOrElse(selectable.get(item), NotFound)
    } yield {
      println(s"Starting! playing '$i' with id $item")
      Ok ~> ResponseString("OK")
    }
    case Path(Seg("work-item" :: item :: "complete" :: Nil)) => for {
      _ <- POST
      _ <- commit
      i <- getOrElse(selectable.get(item), NotFound)
    } yield {
      println(s"Completed! playing '$i' with id $item")
      Ok ~> ResponseString("OK")
    }
    case Path(Seg("binary" :: item :: Nil)) => for {
      _ <- GET
      _ <- commit
      file <- getOrElse(selectable.get(item), NotFound)
    } yield {
      Ok ~> ContentType("audio/mpeg3") ~> ContentLength(file.length().toString) ~> new ResponseStreamer() {
        def stream(os: OutputStream) {
          Streaming.copy(new FileInputStream(file), os)
        }
      }
    }
  }

  def queue = {
    JsonCollection(URI.create("/queue"), Nil, items())
  }

  def items(): List[Item] = {
    val list = selectable.toVector
    val index = Random.nextInt(list.size - 1)
    val item = list(index)
    List(Item(URI.create("/work-item/" + item._1), Nil, Nil))
  }
}

object Streaming {
  def copy(is: InputStream, os: OutputStream, closeOS: Boolean = true) {
    try {
      val buffer = new Array[Byte](1024 * 4)
      var read = 0
      while({read = is.read(buffer); read != -1}) {
        os.write(buffer, 0, read)
      }
    }
    finally {
      if (is != null) is.close()
      if (os != null && closeOS) os.close()
    }
  }
}