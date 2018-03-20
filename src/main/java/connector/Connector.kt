package connector

import com.google.gson.Gson
import com.rabbitmq.client.*
import model.Car
import model.Countries
import model.Invoice
import model.StolenCar

import java.io.IOException
import java.time.Instant

public class Connector {
    private val factory by lazy {
        ConnectionFactory().apply {
            username = "rabbitmq"
            password = "rabbitmq"
            virtualHost = "vhost"
            host = "localhost"
            port = 5672
        }
    }

    private val channel: Channel by lazy {
        factory.newConnection().createChannel()
    }

    private val gson: Gson by lazy {
        Gson()
    }

    init {
        prepare()
    }

    private fun prepare(){
        channel.exchangeDeclare(Constants.carExchangeName, BuiltinExchangeType.DIRECT)
        Countries.values().forEach {
            val queueName = "$it${Constants.carSuffix}"
            channel.queueDeclare(queueName, true,false, false, null)
            channel.queueBind(queueName, Constants.carExchangeName, it.toString())
        }

        channel.exchangeDeclare(Constants.stolenCarExchangeName, BuiltinExchangeType.DIRECT)
        Countries.values().forEach {
            val queueName = "$it${Constants.stolenCArSuffix}"
            channel.queueDeclare(queueName, true,false, false, null)
            channel.queueBind(queueName, Constants.stolenCarExchangeName, it.toString())
        }

        channel.exchangeDeclare(Constants.invoiceExchangeName, BuiltinExchangeType.DIRECT)
        Countries.values().forEach {
            val queueName = "$it${Constants.invoiceSuffix}"
            channel.queueDeclare(queueName, true,false, false, null)
            channel.queueBind(queueName, Constants.invoiceExchangeName, it.toString())
        }
    }
    private fun subscribe(queueName: String, handler: (message: String) -> Unit) {
        channel.basicConsume(queueName, false, "$queueName${Instant.now()}",
                object : DefaultConsumer(channel) {
                    override fun handleDelivery(
                            consumerTag: String,
                            envelope: Envelope,
                            properties: AMQP.BasicProperties,
                            body: ByteArray) {
                        val deliveryTag = envelope.deliveryTag

                        handler(body.toString(Charsets.UTF_8))

                        channel.basicAck(deliveryTag, false)
                    }
                })
    }

    public fun subscribeToQueue(country: Countries, type: Any, handler: (message: String) -> Unit) {
        var queueName = "$country"

        when(type) {
            Car::class.java -> queueName += "car"
            StolenCar::class.java -> queueName += "stolencar"
            Invoice::class.java -> queueName += "invoice"
        }
        subscribe(queueName, handler)
    }

    public fun publishCar(car: Car) {
        try {
            val serializedCar = gson.toJson(car)
            channel.basicPublish("car", car.destinationCountry.toString(), null, serializedCar.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    public fun publishInvoice(invoice: Invoice) {
        try {
            val serializedInvoice = gson.toJson(invoice)
            channel.basicPublish("invoice", invoice.destinationCountry.toString(), null, serializedInvoice.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun publishStolenCar(stolenCar: Car) {
        try {
            val serializedStolenCar = gson.toJson(stolenCar)
            channel.basicPublish("stolenCar", stolenCar.destinationCountry.toString(), null, serializedStolenCar.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}
