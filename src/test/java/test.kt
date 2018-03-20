import connector.Connector
import model.Car
import model.Countries
import model.Countries.AUSTRIA
import org.junit.After
import org.junit.Test

class test {
    val conn by lazy {
        Connector(
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
        conn.subscribeToQueue(AUSTRIA, Car::class.java, { print(it) })

        val car = Car(
                "blaballba",
                Countries.IRELAND,
                Countries.AUSTRIA,
                false
        )

        conn.publishCar(car)

        Thread.sleep(1000)
    }
}