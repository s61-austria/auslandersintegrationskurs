import com.s61.integration.connector.InternationalConnector
import com.s61.integration.connector.model.InternationalCar
import com.s61.integration.connector.model.Countries.AUSTRIA
import com.s61.integration.connector.model.Countries.IRELAND
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
                IRELAND,
                AUSTRIA,
                false
        )

        conn.publishCar(car)

        Thread.sleep(1000)
    }
}