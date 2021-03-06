package com.s61.integration.connector

import com.google.gson.Gson
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import com.s61.integration.model.Countries
import com.s61.integration.model.InternationalCar
import com.s61.integration.model.InternationalInvoice
import com.s61.integration.model.InternationalStolenCar
import java.time.Instant

/**
 * Connect to an international Car Exhange
 * @param username Username for the MQ
 * @param password Password for the mq
 * @param vhost Used virtual host. Default is "vhost"
 * @param host URI for the MQ host
 */
open class InternationalConnector(val username: String,
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

    /**
     * Close the opened connection
     */
    fun close() {
        this.channel.close(0, "thanks!")
    }

    /**
     * Prepare the RabbitMQ
     *
     * This function SHOULD only be run once but it's not a problem to re-ru  it every time
     */
    fun prepare() {
        channel.exchangeDeclare(Constants.carExchangeName, BuiltinExchangeType.FANOUT)
        Countries.values().forEach {
            val queueName = "$it${Constants.carSuffix}"
            channel.queueDeclare(queueName, true, false, false, null)
            channel.queueBind(queueName, Constants.carExchangeName, it.toString())
        }

        channel.exchangeDeclare(Constants.stolenCarExchangeName, BuiltinExchangeType.FANOUT)
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

    /**
     * Subscribe to a queue
     * @param queueName Name of the queue to subscribe to
     * @param handler Function that takes a String to interpret the results
     *          This message will be in JSON format
     * @return Name of the queue subscribed to
     */
    private fun subscribe(queueName: String, handler: (message: String) -> Unit) =
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

    /**
     * Subscribe to a queue in a type-safe way
     * example:
     * ```
     *  subscribeToQueue(Countries.Austria, InternationalCar::class.java, { println(it) })
     * ```
     * @param country The country that you want to receive messages from
     * @param type The type of queue you want to attach to
     * @param handler Function that takes a message (in JSON) and returns null. To be executed on a received message
     * @throws IllegalArgumentException Type does not have a queue
     */
    fun subscribeToQueue(country: Countries, type: Any, handler: (message: String) -> Unit) {
        var queueName = "$country"

        when (type) {
            InternationalCar::class.java -> queueName += Constants.carSuffix
            InternationalStolenCar::class.java -> queueName += Constants.stolenCArSuffix
            InternationalInvoice::class.java -> queueName += Constants.invoiceSuffix
            else -> throw IllegalArgumentException("This type does not have an associated queue")
        }
        subscribe(queueName, handler)
    }

    /**
     * Send an event that a car has left your country
     */
    fun publishCar(internationalCar: InternationalCar) {
        val serializedCar = gson.toJson(internationalCar)
        channel.basicPublish(Constants.carExchangeName, "", null, serializedCar.toByteArray())
    }

    /**
     * Send an event that an invoice is ready for a license plate
     */
    fun publishInvoice(internationalInvoice: InternationalInvoice, country: Countries) {
        val serializedInvoice = gson.toJson(internationalInvoice)
        channel.basicPublish(Constants.invoiceExchangeName, country.name, null, serializedInvoice.toByteArray())
    }

    /**
     * Send an event that a car is reported stolen
     */
    fun publishStolenCar(stolenInternationalCar: InternationalStolenCar) {
        val serializedStolenCar = gson.toJson(stolenInternationalCar)
        channel.basicPublish(Constants.stolenCarExchangeName, "", null, serializedStolenCar.toByteArray())
    }

    enum class Exchanges {
        STOLENCAR,
        CAR,
        INVOICE
    }
}
