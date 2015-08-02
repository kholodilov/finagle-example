import com.twitter.finagle.{Httpx, Service, httpx}
import com.twitter.util._

object ExampleService extends App {
  val service = new Service[httpx.Request, httpx.Response] {
    def apply(req: httpx.Request): Future[httpx.Response] =
      Future.value(httpx.Response(req.version, httpx.Status.Ok))
  }
  val server = Httpx.serve(":8080", service)
  Await.ready(server)
}

object ExampleClient extends App {
  val client: Service[httpx.Request, httpx.Response] = Httpx.newService("localhost:8080")
  val request = httpx.Request(httpx.Method.Get, "/")
  request.host = "localhost"
  val response: Future[httpx.Response] = client(request)
  response.onSuccess { resp: httpx.Response =>
    println("Success: " + resp)
  }
  response.onFailure { e: Throwable =>
    println("Failure: " + e)
  }
  Await.ready(response)
  println("response received")
}
