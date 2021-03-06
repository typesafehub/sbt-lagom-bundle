package api;

import akka.stream.javadsl.Source;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.transport.Method;
import static com.lightbend.lagom.javadsl.api.Service.*;

public interface DebitService extends Service {

  ServiceCall<NotUsed, NotUsed, NotUsed> debit();

  @Override
  default Descriptor descriptor() {
    return named("/debitservice").with(
      restCall(Method.GET,  "/", debit())
    ).withAutoAcl(true);
  }
}
