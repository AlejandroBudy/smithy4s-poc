import cats.Applicative
import cats.effect._
import cats.implicits._
import com.comcast.ip4s._
import org.http4s._
import org.http4s.ember.server._
import org.http4s.implicits._
import smithy4s.Timestamp
import smithy4s.hello._
import smithy4s.http4s.SimpleRestJsonBuilder
import smithy4s.subscriptions._

object HelloWorldImpl {
  def make[F[_]: Applicative](service: Service[F]): HelloWorldService[F] =
    new HelloWorldService[F] {
      override def hello(name: String, town: Option[String]): F[Greeting] =
        service.hello(name, town)
    }
}

trait Service[F[_]] {
  def hello(name: String, town: Option[String]): F[Greeting]
}

object Service {
  def make[F[_]: Applicative]: Service[F] = new Service[F] {
    override def hello(name: String, town: Option[String]): F[Greeting] = town match {
      case Some(t) => Greeting(s"Hello $name from $t!").pure[F]
      case None    => Greeting(s"Hello $name!").pure[F]
    }
  }
}

object SubscriptionServiceImpl extends SubscriptionHttpService[IO] {
  override def get(userid: String): IO[SubscriptionsByUserResponse] = IO.pure(
    SubscriptionsByUserResponse(
      List(Subscription("organization", "repository", Option(Timestamp.nowUTC())))
    )
  )

  override def create(userid: String): IO[Unit] = IO.unit
}

object Routes {
  private val example: Resource[IO, HttpRoutes[IO]] =
    SimpleRestJsonBuilder.routes(HelloWorldImpl.make[IO](Service.make)).resource

  private val subscriptionRoutes: Resource[IO, HttpRoutes[IO]] =
    SimpleRestJsonBuilder.routes(SubscriptionServiceImpl).resource

  private val subscriptionDocs: HttpRoutes[IO] =
    smithy4s.http4s.swagger.docs[IO](SubscriptionHttpService)

  private val docs: HttpRoutes[IO] =
    smithy4s.http4s.swagger.docs[IO](HelloWorldService)

  val all: Resource[IO, HttpRoutes[IO]] = subscriptionRoutes.map(_ <+> subscriptionDocs)
}

object Main extends IOApp.Simple {

  val run = Routes.all
    .flatMap { routes =>
      EmberServerBuilder
        .default[IO]
        .withPort(port"8081")
        .withHost(host"localhost")
        .withHttpApp(routes.orNotFound)
        .build
    }
    .use(_ => IO.never)

}
