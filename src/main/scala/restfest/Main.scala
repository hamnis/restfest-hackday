package restfest

import Implicits._
import org.json4s._
import net.hamnaberg.json.collection._
import dispatch._
import Defaults._
import workorder.WorkOrder
import java.net.URI
import com.ning.http.client.Response


object Main extends App {
  val latch = new java.util.concurrent.CountDownLatch(1)
  work(URI.create(args(0)))

  latch.await()
  sys.exit(0)

  private def work(implicit base: URI) {
    val workItems = for {
      json <- Http(url(base.toString) OK as.String)
      items <- Future { JsonCollection.parse(json).right.toOption.map(_.items).getOrElse(Nil) }
      wo <- Future.sequence(items.map(i => Http(url(base.resolve(i.href).toString) OK as.String)))
    } yield wo.map(w => WorkOrder.parse(w).toOption).flatMap(_.toSeq)

    val firstSound = for {
      items <- workItems
    } yield items.find(wo => wo._type == Player.Type)

    for {
      ss <- firstSound
      res <- ss.flatMap(_.start).map(h => sendPost(h).map(_.getStatusCode)).getOrElse(Future(200))
      _ <- doPlay(res, ss)
    } yield { () }
  }


  private def doPlay(res: Int, workOrder: Option[WorkOrder])(implicit base: URI): Future[Unit] = {
    Future {
      if (res == 200) {
        for {
          wo <- workOrder
          clip <- {
            val sound = wo.as[Sound]
            Some(Player.create(base.resolve(sound.href), sound.volume))
          }
          _ <- {
            clip.play()
            while(clip.isPlaying){ Thread.sleep(100)}
            Some(())
          }
          complete <- wo.complete
        } yield {
          sendPost(complete).onComplete(_ => latch.countDown())
        }
      } else {
        for {
          wo <- workOrder
          fail <- wo.fail
        } yield {
          sendPost(fail).onComplete(_ => latch.countDown())
        }
      }
    }
  }

  private def sendPost(h: URI)(implicit base: URI): dispatch.Future[Response] = {
    Http(url(base.resolve(h).toString).setMethod("POST"))
  }
}
