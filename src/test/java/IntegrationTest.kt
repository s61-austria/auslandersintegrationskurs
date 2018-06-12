import com.s61.integration.connector.InternationalConnector
import com.s61.integration.model.Countries
import com.s61.integration.model.InternationalCar
import com.s61.integration.model.InternationalInvoice
import com.s61.integration.model.InternationalStolenCar
import org.junit.After
import org.junit.Test
import java.time.Instant
import java.util.*

class IntegrationTest {
    val conn by lazy {
        InternationalConnector(
                "rabbitmq",
                "rabbitmq",
                "vhost",
                "ec2-18-197-180-1.eu-central-1.compute.amazonaws.com"
        ).apply { prepare() }
    }

    @After
    fun close() = conn.close()

    /**
     * Send 100 stolen cars to the exchange
     */
    @Test
    fun publishStolenCar(){
        (0..100).map {
            val stolenCar = InternationalStolenCar(
                    "AT-12-34-$it",
                    Countries.AUSTRIA,
                    true
            )
            conn.publishStolenCar(stolenCar)
        }
    }

    /**
     * Send 100 stolen cars to the exchange
     */
    @Test
    fun publishCar(){
        (0..100).map {
            val car = InternationalCar(
                    "AT-12-34-$it",
                    Countries.AUSTRIA,
                    false
            )
            conn.publishCar(car)
        }
    }

    /**
     * Send 100 invoices per country
     */
    @Test
    fun publishInvoices(){
        val random = Random()
        Countries.values().map {country ->
            (0..100).map {
                val invoice = InternationalInvoice(
                        "AT-12-34-$it",
                        random.nextInt(100).toDouble(),
                        random.nextInt(10000).toDouble(),
                        Date.from(Instant.now()),
                        Date.from(Instant.now())
                )

                conn.publishInvoice(invoice, country)
            }
        }
    }
}