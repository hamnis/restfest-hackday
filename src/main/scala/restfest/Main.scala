package restfest

import Implicits._
import org.json4s._
import net.hamnaberg.json.collection._
import dispatch._
import Defaults._
import workorder.WorkOrder
import java.net.URI
import scala.concurrent.Await
import scala.concurrent.duration._


object Main extends App {
  val uri = URI.create(args(0))

  def getWorkItems = for {
    json <- Http(url(uri.toString) OK as.String)
    items <- Future { JsonCollection.parse(json).right.toOption.map(_.items).getOrElse(Nil) }
    wo <- Future.sequence(items.map(i => Http(url(uri.resolve(i.href).toString) OK as.String)))
  } yield wo.map(w => WorkOrder.parse(w).toOption).flatMap(_.toSeq)

  val sounds = for {
    items <- getWorkItems
  } yield items.find(wo => wo._type == Player.Type)

  val result = Await.result(sounds, 10.seconds)

  result.map(wo => wo.as[Sound]).foreach{ s =>
    val res = Player.create(uri.resolve(s.href), s.volume)
    res.play()
  }

}
