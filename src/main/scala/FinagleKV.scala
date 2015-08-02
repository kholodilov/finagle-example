import com.twitter.finagle._
import com.twitter.util._
import org.jboss.netty.handler.codec.http._
import com.twitter.finagle.http.{Http => _, _}
import scala.collection.concurrent.TrieMap

import java.nio.charset.Charset

object FinagleKV extends App {
  val service = new Service[HttpRequest, HttpResponse] {
    val kv = TrieMap.empty[String, String]

    def apply(req: HttpRequest): Future[HttpResponse] = {
      Future {
        val resp = {
          Response(req.getProtocolVersion, HttpResponseStatus.OK)
        }
        val key = req.getUri

        req.getMethod match {
          case HttpMethod.GET =>
            kv.get(key) match {
              case None =>
                resp.setStatus(HttpResponseStatus.NOT_FOUND)
              case Some(value) =>
                resp.setContentString(value)
            }
          case HttpMethod.POST =>
            val value = {
              req.getContent.toString(Charset.forName("UTF-8"))
            }
            kv.update(key, value)
          case HttpMethod.DELETE =>
            kv.remove(key)
          case _ =>
            resp.setStatus(HttpResponseStatus.BAD_REQUEST)
        }
        resp
      }
    }
  }
  val server = Http.serve(":8080", service)
  Await.ready(server)
}