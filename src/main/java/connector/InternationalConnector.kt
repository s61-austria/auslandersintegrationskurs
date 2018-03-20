package connector

import com.google.gson.Gson
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import model.Countries
import model.InternationalCar
import model.InternationalInvoice
import model.InternationalStolenCar
import java.io.IOException
import java.time.Instant

open public class InternationalConnector(val username: String,
                                         val password: String,
                                         val vhost: String,
                                         val host: String) {

    private val factory by lazy {
        ConnectionFactory().apply {
            username = this@InternationalConnector.username
            password = this@InternationalConnector.password
            virtualHost = this@InternationalConnector.vhost
            host = this@InternationalConnector.host
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
            InternationalCar::class.java -> queueName += Constants.carSuffix
            InternationalStolenCar::class.java -> queueName += Constants.stolenCArSuffix
            InternationalInvoice::class.java -> queueName += Constants.invoiceSuffix
        }
        subscribe(queueName, handler)
    }

    public fun publishCar(internationalCar: InternationalCar) {
        try {
            val serializedCar = gson.toJson(internationalCar)
            channel.basicPublish(Constants.carExchangeName, internationalCar.destinationCountry.toString(), null, serializedCar.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    public fun publishInvoice(internationalInvoice: InternationalInvoice) {
        try {
            val serializedInvoice = gson.toJson(internationalInvoice)
            channel.basicPublish(Constants.invoiceExchangeName, internationalInvoice.destinationCountry.toString(), null, serializedInvoice.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun publishStolenCar(stolenInternationalCar: InternationalCar) {
        try {
            val serializedStolenCar = gson.toJson(stolenInternationalCar)
            channel.basicPublish(Constants.stolenCarExchangeName, stolenInternationalCar.destinationCountry.toString(), null, serializedStolenCar.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}
