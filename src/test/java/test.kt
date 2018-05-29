import com.s61.integration.connector.InternationalConnector
import com.s61.integration.model.Countries
import com.s61.integration.model.Countries.AUSTRIA
import com.s61.integration.model.InternationalCar
import org.junit.After
import org.junit.Test

class test {
    val conn by lazy {
        InternationalConnector(
                "rabbitmq",
                "rabbitmq",
                "vhost",
                "localhost"
        ).apply { prepare() }
    }

    @After
    fun tearDown() {
        conn.close()
    }

    @Test
    fun testConnection() {
        conn.subscribeToQueue(AUSTRIA, InternationalCar::class.java, { print(it) })

        val car = InternationalCar(
                "blaballba",
                Countries.IRELAND,
                false
        )

        conn.publishCar(car)

        Thread.sleep(1000)
    }
}