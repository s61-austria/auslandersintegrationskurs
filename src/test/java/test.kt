import connector.Connector
import model.Car
import model.Countries
import org.junit.Test

class test {
    @Test
    fun testConnection() {
        val conn = Connector()

        conn.subscribeToQueue(Countries.AUSTRIA, Car::class.java, {
            print(it)
            assert(it != null)
        })

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