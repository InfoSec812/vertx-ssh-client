package io.vertx.ssh.client;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

/**
 * Represents a socket-like interface to an SSH shell
 * <p>
 * Instances of this class are created on the client side by an {@link SSHClient}
 * <p>
 * It implements both {@link ReadStream} and {@link WriteStream} so it can be used with
 * {@link io.vertx.core.streams.Pump} to pump data with flow control.
 *
 * @author <a href="">Deven Phillips</a>
 */
@VertxGen
public interface SSHSocket extends ReadStream<Buffer>, WriteStream<Buffer> {

  @Override
  SSHSocket exceptionHandler(Handler<Throwable> handler);

  @Override
  SSHSocket handler(Handler<Buffer> handler);

  @Override
  SSHSocket pause();

  @Override
  SSHSocket resume();

  @Override
  SSHSocket endHandler(Handler<Void> endHandler);

  @Override
  SSHSocket write(Buffer data);

  @Override
  SSHSocket setWriteQueueMaxSize(int maxSize);

  @Override
  SSHSocket drainHandler(Handler<Void> handler);

  /**
   * When a {@code NetSocket} is created it automatically registers an event handler with the event bus, the ID of that
   * handler is given by {@code writeHandlerID}.
   * <p>
   * Given this ID, a different event loop can send a buffer to that event handler using the event bus and
   * that buffer will be received by this instance in its own event loop and written to the underlying connection. This
   * allows you to write data to other connections which are owned by different event loops.
   *
   * @return the write handler ID
   */
  String writeHandlerID();

  /**
   * Write a {@link String} to the connection, encoded in UTF-8.
   *
   * @param str  the string to write
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SSHSocket write(String str);

  /**
   * Write a {@link String} to the connection, encoded using the encoding {@code enc}.
   *
   * @param str  the string to write
   * @param enc  the encoding to use
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SSHSocket write(String str, String enc);
  
  /**
   * Write the provided {@link Buffer} to the specified filename on the SSH server
   * @param filename  file name of the file to send
   * @param resultHandler  handler
   * @param fileData A Buffer which contains the data to be written to the target file.
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SSHSocket writeFile(String filename, Buffer fileData, Handler<AsyncResult<Void>> resultHandler);
  
  /**
   * Retrieve a file from the remote SSH server using the SCP channel asynchronously. The handler will be
   * called as data from the file is read asynchronously.
   * @param filename The full path and filename of the file to be retrieved from the remote host
   * @param resultHandler  An async handler for file data as it is read. The result is a Buffer which can be 
   *                        {@link Pump}ed to a {@link WriteStream}
   * @return a reference to this {@link SSHSocket} so that this API can be used fluently.
   */
  @Fluent
  SSHSocket readFile(String filename, Handler<AsyncResult<Buffer>> resultHandler);

  /**
   * Close the NetSocket
   */
  void close();

  /**
   * Set a handler that will be called when the SSHSocket is closed
   *
   * @param handler  the handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SSHSocket closeHandler(Handler<Void> handler);

}
