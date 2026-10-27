package com.izivia.ocpp.transport

import com.izivia.ocpp.operation.information.ChargingStationConfig
import com.izivia.ocpp.operation.information.RequestMetadata
import kotlin.reflect.KClass

interface ServerTransport {

    fun <T, P : Any> sendMessageClass(clazz: KClass<P>, csOcppId: String, action: String, message: T): P

    fun <T : Any, P> receiveMessageClass(clazz: KClass<T>,
                                         action: String,
                                         ocppVersion: OcppVersion,
                                         onAction: (RequestMetadata, T) -> P,
                                         accept: (String) -> ChargingStationConfig)

    fun canSendToChargingStation(chargingStationConfig: ChargingStationConfig): Boolean

    /**
     * Gracefully shuts the transport down: stop accepting connections and calls, then wait for
     * the pending calls to complete before closing the open connections.
     *
     * The shutdown is irreversible and may block until the pending calls time out. It does not
     * stop the HTTP server the transport is mounted on: stop that server afterwards.
     *
     * The default implementation does nothing, for transports with no pending call to drain.
     */
    fun shutdown() {}

}

inline fun <T, reified P : Any> ServerTransport.sendMessage(csOcppId: String, action: String, message: T): P =
    sendMessageClass(P::class, csOcppId, action, message)

inline fun <reified T : Any, P> ServerTransport.receiveMessage(
    action: String,
    ocppVersion: OcppVersion,
    noinline onAction: (RequestMetadata, T) -> P,
    noinline accept: (String) -> ChargingStationConfig
) = receiveMessageClass(T::class, action, ocppVersion, onAction, accept)
