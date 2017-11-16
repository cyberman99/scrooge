/**
 * Generated by Scrooge
 *   version: ?
 *   rev: ?
 *   built at: ?
 */
package com.twitter.scrooge.test.gold.thriftscala

import com.twitter.finagle.{
  service => ctfs,
  Filter => finagle$Filter,
  Service => finagle$Service,
  thrift => _,
  _
}
import com.twitter.finagle.stats.{Counter, NullStatsReceiver, StatsReceiver}
import com.twitter.finagle.thrift.RichServerParam
import com.twitter.io.Buf
import com.twitter.scrooge._
import com.twitter.util.{Future, Return, Throw, Throwables}
import java.nio.ByteBuffer
import java.util.Arrays
import org.apache.thrift.protocol._
import org.apache.thrift.TApplicationException
import org.apache.thrift.transport.TMemoryInputTransport
import scala.collection.mutable.{
  ArrayBuffer => mutable$ArrayBuffer, HashMap => mutable$HashMap}
import scala.collection.{Map, Set}

import scala.language.higherKinds


@javax.annotation.Generated(value = Array("com.twitter.scrooge.Compiler"))
class GoldService$FinagleService(
  iface: GoldService[Future],
  serverParam: RichServerParam
) extends com.twitter.finagle.Service[Array[Byte], Array[Byte]] {
  import GoldService._

  @deprecated("Use com.twitter.finagle.thrift.RichServerParam", "2017-08-16")
  def this(
    iface: GoldService[Future],
    protocolFactory: TProtocolFactory,
    stats: StatsReceiver = NullStatsReceiver,
    maxThriftBufferSize: Int = Thrift.param.maxThriftBufferSize,
    serviceName: String = "GoldService"
  ) = this(iface, RichServerParam(protocolFactory, serviceName, maxThriftBufferSize, stats))

  @deprecated("Use com.twitter.finagle.thrift.RichServerParam", "2017-08-16")
  def this(
    iface: GoldService[Future],
    protocolFactory: TProtocolFactory,
    stats: StatsReceiver,
    maxThriftBufferSize: Int
  ) = this(iface, protocolFactory, stats, maxThriftBufferSize, "GoldService")

  @deprecated("Use com.twitter.finagle.thrift.RichServerParam", "2017-08-16")
  def this(
    iface: GoldService[Future],
    protocolFactory: TProtocolFactory
  ) = this(iface, protocolFactory, NullStatsReceiver, Thrift.param.maxThriftBufferSize)

  def serviceName: String = serverParam.serviceName
  private[this] def responseClassifier: ctfs.ResponseClassifier = serverParam.responseClassifier
  private[this] def stats: StatsReceiver = serverParam.serverStats

  private[this] def protocolFactory: TProtocolFactory = serverParam.protocolFactory
  private[this] def maxReusableBufferSize: Int = serverParam.maxThriftBufferSize

  private[this] val tlReusableBuffer = TReusableBuffer(maxThriftBufferSize = maxReusableBufferSize)

  protected val serviceMap = new mutable$HashMap[String, finagle$Service[(TProtocol, Int), Array[Byte]]]()

  protected def addService(name: String, service: finagle$Service[(TProtocol, Int), Array[Byte]]): Unit = {
    serviceMap(name) = service
  }

  final protected def exception(name: String, seqid: Int, code: Int, message: String): Buf = {
    val x = new TApplicationException(code, message)
    val memoryBuffer = tlReusableBuffer.get()
    try {
      val oprot = protocolFactory.getProtocol(memoryBuffer)

      oprot.writeMessageBegin(new TMessage(name, TMessageType.EXCEPTION, seqid))
      x.write(oprot)
      oprot.writeMessageEnd()
      oprot.getTransport().flush()

      // make a copy of the array of bytes to construct a new buffer because memoryBuffer is reusable
      Buf.ByteArray.Shared(memoryBuffer.getArray(), 0, memoryBuffer.length())
    } finally {
      tlReusableBuffer.reset()
    }
  }

  final protected def reply(name: String, seqid: Int, result: ThriftStruct): Buf = {
    val memoryBuffer = tlReusableBuffer.get()
    try {
      val oprot = protocolFactory.getProtocol(memoryBuffer)

      oprot.writeMessageBegin(new TMessage(name, TMessageType.REPLY, seqid))
      result.write(oprot)
      oprot.writeMessageEnd()
      oprot.getTransport().flush()

      // make a copy of the array of bytes to construct a new buffer because memoryBuffer is reusable
      Buf.ByteArray.Shared(memoryBuffer.getArray(), 0, memoryBuffer.length())
    } finally {
      tlReusableBuffer.reset()
    }
  }

  final def apply(request: Array[Byte]): Future[Array[Byte]] = {
    val inputTransport = new TMemoryInputTransport(request)
    val iprot = protocolFactory.getProtocol(inputTransport)

    try {
      val msg = iprot.readMessageBegin()
      val service = serviceMap.get(msg.name)
      service match {
        case _root_.scala.Some(svc) =>
          svc(iprot, msg.seqid)
        case _ =>
          TProtocolUtil.skip(iprot, TType.STRUCT)
          Future.value(Buf.ByteArray.Owned.extract(
            exception(msg.name, msg.seqid, TApplicationException.UNKNOWN_METHOD,
              "Invalid method name: '" + msg.name + "'")))
      }
    } catch {
      case e: Exception => Future.exception(e)
    }
  }

  private object ThriftMethodStats {
    def apply(stats: StatsReceiver): ThriftMethodStats =
      ThriftMethodStats(
        stats.counter("requests"),
        stats.counter("success"),
        stats.counter("failures"),
        stats.scope("failures")
      )
  }

  private case class ThriftMethodStats(
    requestsCounter: Counter,
    successCounter: Counter,
    failuresCounter: Counter,
    failuresScope: StatsReceiver
  )

  private def missingResult(name: String): TApplicationException = {
    new TApplicationException(
      TApplicationException.MISSING_RESULT,
      name + " failed: unknown result"
    )
  }

  private def setServiceName(ex: Throwable): Throwable =
    if (this.serviceName == "") ex
    else {
      ex match {
        case se: SourcedException =>
          se.serviceName = this.serviceName
          se
        case _ => ex
      }
    }

  private def recordResponse(reqRep: ctfs.ReqRep, methodStats: ThriftMethodStats): Unit = {
    val responseClass = responseClassifier.applyOrElse(reqRep, ctfs.ResponseClassifier.Default)
    responseClass match {
      case ctfs.ResponseClass.Successful(_) =>
        methodStats.successCounter.incr()
      case ctfs.ResponseClass.Failed(_) =>
        methodStats.failuresCounter.incr()
        reqRep.response match {
          case Throw(ex) =>
            methodStats.failuresScope.counter(Throwables.mkString(ex): _*).incr()
          case _ =>
        }
    }
  }

  final protected def perMethodStatsFilter(
    method: ThriftMethod
  ): finagle$Filter[(TProtocol, Int), Array[Byte], (TProtocol, Int), RichResponse[method.Args, method.Result]] = {
    val methodStats = ThriftMethodStats((if (serviceName != "") stats.scope(serviceName) else stats).scope(method.name))
    new finagle$Filter[(TProtocol, Int), Array[Byte], (TProtocol, Int), RichResponse[method.Args, method.Result]] {
      def apply(
        req: (TProtocol, Int),
        service: finagle$Service[(TProtocol, Int), RichResponse[method.Args, method.Result]]
      ): Future[Array[Byte]] = {
        methodStats.requestsCounter.incr()
        service(req).transform {
          case Return(value) =>
            value match {
              case SuccessfulResult(req, _, result) =>
                recordResponse(ctfs.ReqRep(req, _root_.com.twitter.util.Return(result.successField.get)), methodStats)
              case ProtocolException(req, _, exp) =>
                recordResponse(ctfs.ReqRep(req, _root_.com.twitter.util.Throw(exp)), methodStats)
              case ThriftExceptionResult(req, _, result) =>
                val rep = result.firstException match {
                  case Some(exp) => setServiceName(exp)
                  case None => missingResult(serviceName)
                }
                recordResponse(ctfs.ReqRep(req, _root_.com.twitter.util.Throw(rep)), methodStats)
            }
            Future.value(Buf.ByteArray.Owned.extract(value.response))
          case t @ Throw(_) =>
            recordResponse(ctfs.ReqRep(req, t), methodStats)
            Future.const(t.cast[Array[Byte]])
        }
      }
    }
  }
  // ---- end boilerplate.

  addService("doGreatThings", {
    val statsFilter: finagle$Filter[(TProtocol, Int), Array[Byte], (TProtocol, Int), RichResponse[DoGreatThings.Args, DoGreatThings.Result]] = perMethodStatsFilter(DoGreatThings)

    val methodService = new finagle$Service[DoGreatThings.Args, DoGreatThings.SuccessType] {
      def apply(args: DoGreatThings.Args): Future[DoGreatThings.SuccessType] = {
        iface.doGreatThings(args.request)
      }
    }

    val protocolExnFilter = new SimpleFilter[(TProtocol, Int), RichResponse[DoGreatThings.Args, DoGreatThings.Result]] {
      def apply(
        request: (TProtocol, Int),
        service: finagle$Service[(TProtocol, Int), RichResponse[DoGreatThings.Args, DoGreatThings.Result]]
      ): Future[RichResponse[DoGreatThings.Args, DoGreatThings.Result]] = {
        val iprot = request._1
        val seqid = request._2
        val res = service(request)
        res.transform {
          case _root_.com.twitter.util.Throw(e: TProtocolException) =>
            iprot.readMessageEnd()
            Future.value(
              ProtocolException(
                null,
                exception("doGreatThings", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage),
                new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage)))
          case _ =>
            res
        }
      }
    }

    val serdeFilter = new finagle$Filter[(TProtocol, Int), RichResponse[DoGreatThings.Args, DoGreatThings.Result], DoGreatThings.Args, DoGreatThings.SuccessType] {
      def apply(
        request: (TProtocol, Int),
        service: finagle$Service[DoGreatThings.Args, DoGreatThings.SuccessType]
      ): Future[RichResponse[DoGreatThings.Args, DoGreatThings.Result]] = {
        val iprot = request._1
        val seqid = request._2
        val args = DoGreatThings.Args.decode(iprot)
        iprot.readMessageEnd()
        val res = service(args)
        res.transform {
          case _root_.com.twitter.util.Return(value) =>
            val methodResult = DoGreatThings.Result(success = Some(value))
            Future.value(
              SuccessfulResult(
                args,
                reply("doGreatThings", seqid, methodResult),
                methodResult))
          case _root_.com.twitter.util.Throw(e: com.twitter.scrooge.test.gold.thriftscala.OverCapacityException) => {
            val methodResult = DoGreatThings.Result(ex = Some(e))
            Future.value(
              ThriftExceptionResult(
                args,
                reply("doGreatThings", seqid, methodResult),
                methodResult))
          }
          case t @ _root_.com.twitter.util.Throw(_) =>
            Future.const(t.cast[RichResponse[DoGreatThings.Args, DoGreatThings.Result]])
        }
      }
    }

    statsFilter.andThen(protocolExnFilter).andThen(serdeFilter).andThen(methodService)
  })
}
