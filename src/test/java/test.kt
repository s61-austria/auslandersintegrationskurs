import connector.InternationalConnector
import model.InternationalCar
import model.Countries.AUSTRIA
import model.Countries.IRELAND
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