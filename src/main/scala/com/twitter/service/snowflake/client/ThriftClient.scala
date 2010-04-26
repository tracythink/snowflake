/** Copyright 2010 Twitter, Inc. */
package com.twitter.service.snowflake.client

import org.apache.thrift.TException
import org.apache.thrift.protocol.{TBinaryProtocol, TProtocol}
import org.apache.thrift.transport.{TFramedTransport, TSocket, TTransport, TTransportException}
import com.twitter.service.snowflake.gen.Snowflake
import net.lag.configgy.ConfigMap
import net.lag.logging.Logger
import scala.reflect.Manifest


class ThriftClient(implicit man: Manifest[Snowflake.Client]) {
  def newClient(protocol: TProtocol)(implicit m: Manifest[Snowflake.Client]): Snowflake.Client = {
    val constructor = m.erasure.
    getConstructor(classOf[TProtocol])
    constructor.newInstance(protocol).asInstanceOf[Snowflake.Client]
  }

  val log = Logger.get
  /**
   * @param soTimeoutMS the Socket timeout for both connect and read.
   */
  def create(hostname: String, port: Int, soTimeoutMS: Int): (TTransport, Snowflake.Client) = {
    val socket = new TSocket(hostname, port, soTimeoutMS)
    val transport = new TFramedTransport(socket)
    val protocol: TProtocol  = new TBinaryProtocol(transport)

    transport.open()
    log.debug("creating new TSocket: remote-host = %s remote-port = %d local-port = %d timeout = %d",
      hostname, socket.getSocket.getPort, socket.getSocket.getLocalPort, soTimeoutMS)
    (transport, newClient(protocol))
  }
}

