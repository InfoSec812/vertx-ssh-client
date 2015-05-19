package io.vertx.ssh.client.impl;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.Log4jLogDelegateFactory;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ssh.client.ReadableOutputStream;
import io.vertx.ssh.client.SSHSocket;
import io.vertx.ssh.client.WriteableInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import org.mvel2.util.ThisLiteral;

public class SSHSocketImpl implements SSHSocket {

  private static final byte[] NULL = {0};

  private Handler<Throwable> exHandler;
  private Handler<Buffer> dataHandler;
  private Handler<Void> endHandler;
  private Handler<Void> drainHandler;
  private final ReadableOutputStream inFromSSH;
  private final WriteableInputStream outToSSH;
  private final Vertx vertx;
  private final Session session;
  private final JSch jsch;
  private final Channel shell;
  private final String writeHandlerID;
  private final MessageConsumer registration;
  private boolean paused = false;

  public SSHSocketImpl(Vertx vertx, String host, String user, String pass, String key, int port) throws JSchException {
    super();
    this.vertx = vertx;
    jsch = new JSch();
    if (key != null) {
      jsch.addIdentity(key);
      session = jsch.getSession(user, host, port);
    } else {
      session = jsch.getSession(user, host, port);
      session.setPassword(pass);
    }
    shell = session.openChannel("shell");
    inFromSSH = new ReadableOutputStream(vertx);
    outToSSH = new WriteableInputStream(vertx);
    shell.setInputStream(outToSSH);
    shell.setOutputStream(inFromSSH);
    writeHandlerID = UUID.randomUUID().toString();
    Handler<Message<Buffer>> writeHandler = msg -> write(msg.body());
    registration = vertx.eventBus().<Buffer>localConsumer(writeHandlerID).handler(writeHandler);
  }

  @Override
  public SSHSocket exceptionHandler(Handler<Throwable> handler) {
    this.exHandler = handler;
    return this;
  }

  @Override
  public SSHSocket handler(Handler<Buffer> handler) {
    this.dataHandler = handler;
    return this;
  }

  @Override
  public SSHSocket pause() {
    this.paused = true;
    inFromSSH.pause();
    return this;
  }

  @Override
  public SSHSocket resume() {
    inFromSSH.resume();
    this.paused = false;
    return this;
  }

  @Override
  public SSHSocket endHandler(Handler<Void> endHandler) {
    this.endHandler = endHandler;
    return this;
  }

  @Override
  public SSHSocket write(Buffer data) {
    outToSSH.write(data);
    return this;
  }

  @Override
  public SSHSocket setWriteQueueMaxSize(int maxSize) {
    outToSSH.setWriteQueueMaxSize(maxSize);
    return this;
  }

  @Override
  public SSHSocket drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    return this;
  }

  @Override
  public String writeHandlerID() {
    return writeHandlerID;
  }

  @Override
  public SSHSocket write(String str) {
    outToSSH.write(Buffer.buffer(str));
    return this;
  }

  @Override
  public SSHSocket write(String str, String enc) {
    outToSSH.write(Buffer.buffer(str, enc));
    return this;
  }

  @Override
  public SSHSocket writeFile(final String filename, final Buffer fileData, final Handler<AsyncResult<Void>> resultHandler) {
    vertx.executeBlocking(future -> {
      try {
        final String scpCmd = "scp -f " + filename;
        Channel fileChannel = session.openChannel("exec");

        ((ChannelExec) fileChannel).setCommand(scpCmd);
        OutputStream fileOut = fileChannel.getOutputStream();
        InputStream fileIn = fileChannel.getExtInputStream();

        fileChannel.connect();

        int response = checkAck(fileIn);
        if (response != 0) {
          throw new IOException("Failed to execute remote SCP command: " + response);
        }

        fileOut.write("C0644 ".getBytes());
        fileOut.write((fileData.length() + "").getBytes());
        fileOut.write(" ".getBytes());
        fileOut.write(fileData.getBytes());
        fileOut.write(NULL, 0, 1);
        fileOut.flush();

        response = checkAck(fileIn);
        if (response != 0) {
          throw new IOException("Unable to write file data: " + response);
        }

        fileOut.close();
        fileIn.close();
        fileChannel.disconnect();
        future.complete();
      } catch (JSchException | IOException e) {
        future.fail(e);
      }
    }, resultHandler);
    return this;
  }

  private static int checkAck(InputStream in) throws IOException {
    int b = in.read();
    // b may be 0 for success,
    //          1 for error,
    //          2 for fatal error,
    //          -1
    if (b == 0) {
      return b;
    }
    if (b == -1) {
      return b;
    }

    if (b == 1 || b == 2) {
      StringBuffer sb = new StringBuffer();
      int c;
      do {
        c = in.read();
        sb.append((char) c);
      } while (c != '\n');
      if (b == 1) { // error
        System.out.print(sb.toString());
      }
      if (b == 2) { // fatal error
        System.out.print(sb.toString());
      }
    }
    return b;
  }

  @Override
  public SSHSocket readFile(final String filename, final Handler<Buffer> resultHandler) {
    vertx.executeBlocking(future -> {
      try {
        final String scpCmd = "scp -f " + filename;
        Channel fileChannel = session.openChannel("exec");
        ((ChannelExec) fileChannel).setCommand(scpCmd);

        OutputStream fileOut = fileChannel.getOutputStream();
        InputStream fileIn = fileChannel.getInputStream();

        fileChannel.connect();

        fileOut.write(NULL, 0, 1);
        fileOut.flush();

        while (true) {
          int c = checkAck(fileIn);
          if (c != 'C') {
            break;
          }

          byte[] buf = new byte[1024];
          fileIn.read(buf, 0, 5);

          long filesize = 0L;
          while (true) {
            if (fileIn.read(buf, 0, 1) < 0) {
              // error
              break;
            }
            if (buf[0] == ' ') {
              break;
            }
            filesize = filesize * 10L + (long) (buf[0] - '0');
          }

          String file = null;
          while (true) {
            int readLen = fileIn.read(buf);
            resultHandler.handle(Buffer.buffer(buf));
            if (buf[readLen-1] == (byte) 0x0a) {
              break;
            }
          }

          //System.out.println("filesize="+filesize+", file="+file);
          // send '\0'
          fileOut.write(NULL, 0, 1);
          fileOut.flush();
          fileOut.close();
          fileIn.close();
          fileChannel.disconnect();
          future.complete();
        }
      } catch (JSchException | IOException e) {
        future.fail(e);
      }
    }, res -> {
      if (res.failed()) {
        LoggerFactory.getLogger(this.getClass()).error(res.cause().getLocalizedMessage(), res.cause().getCause());
      }
    });
    return this;
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SSHSocket closeHandler(Handler<Void> handler) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean writeQueueFull() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
