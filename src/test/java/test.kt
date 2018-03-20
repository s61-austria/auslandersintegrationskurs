import model.Car
import model.Countries
import org.jetbrains.annotations.TestOnly
import org.junit.Test

class test {
    val connector = Connector()

    @Test
    fun testConnection(){
        connector.prepare()

        connector.subscribeToQueue(Countries.AUSTRIA, Car::class.java,{ print(it)})

        val car = Car(
                "blaballba",
                Countries.IRELAND,
                Countries.AUSTRIA,
                false
        )

        connector.publishCar(car)
    }
}