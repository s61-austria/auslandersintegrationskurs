package connector

import com.google.gson.Gson
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import model.Car
import model.Countries
import model.Invoice
import model.StolenCar
import java.io.IOException
import java.time.Instant

open public class Connector(val username: String,
                       val password: String,
                       val vhost: String,
                       val host: String) {

    private val factory by lazy {
        ConnectionFactory().apply {
            username = this@Connector.username
            password = this@Connector.password
            virtualHost = this@Connector.vhost
            host = this@Connector.host
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

    public fun close() {
        this.channel.close(0, "thanks!")
    }

    public fun prepare() {
        channel.exchangeDeclare(Constants.carExchangeName, BuiltinExchangeType.DIRECT)
        Countries.values().forEach {
            val queueName = "$it${Constants.carSuffix}"
            channel.queueDeclare(queueName, true, false, false, null)
            channel.queueBind(queueName, Constants.carExchangeName, it.toString())
        }

        channel.exchangeDeclare(Constants.stolenCarExchangeName, BuiltinExchangeType.DIRECT)
        Countries.values().forEach {
            val queueName = "$it${Constants.stolenCArSuffix}"
            channel.queueDeclare(queueName, true, false, false, null)
            channel.queueBind(queueName, Constants.stolenCarExchangeName, it.toString())
        }

        channel.exchangeDeclare(Constants.invoiceExchangeName, BuiltinExchangeType.DIRECT)
        Countries.values().forEach {
            val queueName = "$it${Constants.invoiceSuffix}"
            channel.queueDeclare(queueName, true, false, false, null)
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

        when (type) {
            Car::class.java -> queueName += Constants.carSuffix
            StolenCar::class.java -> queueName += Constants.stolenCArSuffix
            Invoice::class.java -> queueName += Constants.invoiceSuffix
        }
        subscribe(queueName, handler)
    }

    public fun publishCar(car: Car) {
        try {
            val serializedCar = gson.toJson(car)
            channel.basicPublish(Constants.carExchangeName, car.destinationCountry.toString(), null, serializedCar.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    public fun publishInvoice(invoice: Invoice) {
        try {
            val serializedInvoice = gson.toJson(invoice)
            channel.basicPublish(Constants.invoiceExchangeName, invoice.destinationCountry.toString(), null, serializedInvoice.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun publishStolenCar(stolenCar: Car) {
        try {
            val serializedStolenCar = gson.toJson(stolenCar)
            channel.basicPublish(Constants.stolenCarExchangeName, stolenCar.destinationCountry.toString(), null, serializedStolenCar.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}
