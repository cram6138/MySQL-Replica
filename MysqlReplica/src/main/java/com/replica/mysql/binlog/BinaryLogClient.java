package com.replica.mysql.binlog;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.replica.binlog.network.ptotocol.ErrorPacket;
import com.replica.binlog.network.ptotocol.GreetingPacket;
import com.replica.binlog.network.ptotocol.PacketChannel;
import com.replica.binlog.network.ptotocol.ResultSetRowPacket;
import com.replica.binlog.network.ptotocol.command.AuthenticateCommand;
import com.replica.binlog.network.ptotocol.command.AuthenticateNativePasswordCommand;
import com.replica.binlog.network.ptotocol.command.Command;
import com.replica.binlog.network.ptotocol.command.DumpBinaryLogCommand;
import com.replica.binlog.network.ptotocol.command.DumpBinaryLogGtidCommand;
import com.replica.binlog.network.ptotocol.command.PingCommand;
import com.replica.binlog.network.ptotocol.command.QueryCommand;
import com.replica.binlog.network.ptotocol.command.SSLRequestCommand;
import com.replica.mysql.binlog.event.Event;
import com.replica.mysql.binlog.event.EventHeader;
import com.replica.mysql.binlog.event.EventHeaderV4;
import com.replica.mysql.binlog.event.EventType;
import com.replica.mysql.binlog.event.GtidEventData;
import com.replica.mysql.binlog.event.QueryEventData;
import com.replica.mysql.binlog.event.RotateEventData;
import com.replica.mysql.binlog.event.deserialization.ChecksumType;
import com.replica.mysql.binlog.event.deserialization.EventDataDeserializationException;
import com.replica.mysql.binlog.event.deserialization.EventDataDeserializer;
import com.replica.mysql.binlog.event.deserialization.EventDeserializer;
import com.replica.mysql.binlog.event.deserialization.GtidEventDataDeserializer;
import com.replica.mysql.binlog.event.deserialization.QueryEventDataDeserializer;
import com.replica.mysql.binlog.event.deserialization.RotateEventDataDeserializer;
import com.replica.mysql.binlog.event.deserialization.EventDeserializer.EventDataWrapper;
import com.replica.mysql.binlog.io.ByteArrayInputStream;
import com.replica.mysql.binlog.network.AuthenticationException;
import com.replica.mysql.binlog.network.ClientCapabilities;
import com.replica.mysql.binlog.network.DefaultSSLSocketFactory;
import com.replica.mysql.binlog.network.Packet;
import com.replica.mysql.binlog.network.SSLMode;
import com.replica.mysql.binlog.network.SSLSocketFactory;
import com.replica.mysql.binlog.network.ServerException;
import com.replica.mysql.binlog.network.SocketFactory;
import com.replica.mysql.binlog.network.TLSHostnameVerifier;

public class BinaryLogClient {
	private static final SSLSocketFactory DEFAULT_REQUIRED_SSL_MODE_SOCKET_FACTORY = new DefaultSSLSocketFactory() {

        @Override
        protected void initSSLContext(SSLContext sc) throws GeneralSecurityException {
            sc.init(null, new TrustManager[]{
                new X509TrustManager() {

                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                        throws CertificateException { }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                        throws CertificateException { }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
            }, null);
        }
    };
	private static final SSLSocketFactory DEFAULT_VERIFY_CA_SSL_MODE_SOCKET_FACTORY = new DefaultSSLSocketFactory();
	
	private static final int MAX_PACKET_LENGTH = 16777215;
	private final Logger logger = Logger.getLogger(getClass().getName());
	private final String hostname;
    private final int port;
    private final String schema;
    private final String username;
    private final String password;

    
    private boolean blocking = true;
    private long serverId = 65535;
    private volatile String binlogFilename;
    private volatile long binlogPosition = 4;
    private volatile long connectionId;
    private SSLMode sslMode = SSLMode.DISABLED;

    private GtidSet gtidSet;
    private final Object gtidSetAccessLock = new Object();
    private boolean gtidSetFallbackToPurged;
    private boolean useBinlogFilenamePositionInGtidMode;
    private String gtid;
    private boolean tx;

    private EventDeserializer eventDeserializer = new EventDeserializer();

    private final List<EventListener> eventListeners = new CopyOnWriteArrayList<EventListener>();
    private final List<LifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<LifecycleListener>();

    private SocketFactory socketFactory;
    private SSLSocketFactory sslSocketFactory;

    private volatile PacketChannel channel;
    private volatile boolean connected;

    private ThreadFactory threadFactory;

    private boolean keepAlive = true;
    private long keepAliveInterval = TimeUnit.MINUTES.toMillis(1);

    private long heartbeatInterval;
    private volatile long eventLastSeen;

    private long connectTimeout = TimeUnit.SECONDS.toMillis(3);

    private volatile ExecutorService keepAliveThreadExecutor;

    private final Lock connectLock = new ReentrantLock();
    private final Lock keepAliveThreadExecutorLock = new ReentrantLock();

    /**
     * Alias for BinaryLogClient("localhost", 3306, &lt;no schema&gt; = null, username, password).
     * @see BinaryLogClient#BinaryLogClient(String, int, String, String, String)
     */
    public BinaryLogClient(String username, String password) {
        this("localhost", 3306, null, username, password);
    }

    /**
     * Alias for BinaryLogClient("localhost", 3306, schema, username, password).
     * @see BinaryLogClient#BinaryLogClient(String, int, String, String, String)
     */
    public BinaryLogClient(String schema, String username, String password) {
        this("localhost", 3306, schema, username, password);
    }

    /**
     * Alias for BinaryLogClient(hostname, port, &lt;no schema&gt; = null, username, password).
     * @see BinaryLogClient#BinaryLogClient(String, int, String, String, String)
     */
    public BinaryLogClient(String hostname, int port, String username, String password) {
        this(hostname, port, null, username, password);
    }

    /**
     * @param hostname mysql server hostname
     * @param port mysql server port
     * @param schema database name, nullable. Note that this parameter has nothing to do with event filtering. It's
     * used only during the authentication.
     * @param username login name
     * @param password password
     */
    public BinaryLogClient(String hostname, int port, String schema, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.schema = schema;
        this.username = username;
        this.password = password;
    }
    
    boolean isKeepAliveThreadRunning() {
        try {
            keepAliveThreadExecutorLock.lock();
            return keepAliveThreadExecutor != null && !keepAliveThreadExecutor.isShutdown();
        } finally {
            keepAliveThreadExecutorLock.unlock();
        }
    }
    
    /**
     * Connect to the replication stream. Note that this method blocks until disconnected.
     * @throws AuthenticationException if authentication fails
     * @throws ServerException if MySQL server responds with an error
     * @throws IOException if anything goes wrong while trying to connect
     */
    public void connect() throws IOException {
        if (!connectLock.tryLock()) {
            throw new IllegalStateException("BinaryLogClient is already connected");
        }
        boolean notifyWhenDisconnected = false;
        try {
            Callable cancelDisconnect = null;
            try {
                try {
                    long start = System.currentTimeMillis();
                    channel = openChannel();
                    if (connectTimeout > 0 && !isKeepAliveThreadRunning()) {
                        cancelDisconnect = scheduleDisconnectIn(connectTimeout -
                            (System.currentTimeMillis() - start));
                    }
                    if (channel.getInputStream().peek() == -1) {
                        throw new EOFException();
                    }
                } catch (IOException e) {
                    throw new IOException("Failed to connect to MySQL on " + hostname + ":" + port +
                        ". Please make sure it's running.", e);
                }
                GreetingPacket greetingPacket = receiveGreeting();
                authenticate(greetingPacket);
                connectionId = greetingPacket.getThreadId();
                if ("".equals(binlogFilename)) {
                    synchronized (gtidSetAccessLock) {
                        if (gtidSet != null && "".equals(gtidSet.toString()) && gtidSetFallbackToPurged) {
                            gtidSet = new GtidSet(fetchGtidPurged());
                        }
                    }
                }
                if (binlogFilename == null) {
                    fetchBinlogFilenameAndPosition();
                }
                if (binlogPosition < 4) {
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.warning("Binary log position adjusted from " + binlogPosition + " to " + 4);
                    }
                    binlogPosition = 4;
                }
                ChecksumType checksumType = fetchBinlogChecksum();
                if (checksumType != ChecksumType.NONE) {
                    confirmSupportOfChecksum(checksumType);
                }
                if (heartbeatInterval > 0) {
                    enableHeartbeat();
                }
                gtid = null;
                tx = false;
                requestBinaryLogStream();
            } catch (IOException e) {
                disconnectChannel();
                throw e;
            } finally {
                if (cancelDisconnect != null) {
                    try {
                        cancelDisconnect.call();
                    } catch (Exception e) {
                        if (logger.isLoggable(Level.WARNING)) {
                            logger.warning("\"" + e.getMessage() +
                                "\" was thrown while canceling scheduled disconnect call");
                        }
                    }
                }
            }
            connected = true;
            notifyWhenDisconnected = true;
            if (logger.isLoggable(Level.INFO)) {
                String position;
                synchronized (gtidSetAccessLock) {
                    position = gtidSet != null ? gtidSet.toString() : binlogFilename + "/" + binlogPosition;
                }
                logger.info("Connected to " + hostname + ":" + port + " at " + position +
                    " (" + (blocking ? "sid:" + serverId + ", " : "") + "cid:" + connectionId + ")");
            }
            for (LifecycleListener lifecycleListener : lifecycleListeners) {
                lifecycleListener.onConnect(this);
            }
            if (keepAlive && !isKeepAliveThreadRunning()) {
                spawnKeepAliveThread();
            }
            ensureEventDataDeserializer(EventType.ROTATE, RotateEventDataDeserializer.class);
            synchronized (gtidSetAccessLock) {
                if (gtidSet != null) {
                    ensureEventDataDeserializer(EventType.GTID, GtidEventDataDeserializer.class);
                    ensureEventDataDeserializer(EventType.QUERY, QueryEventDataDeserializer.class);
                }
            }
            listenForEventPackets();
        } finally {
            connectLock.unlock();
            if (notifyWhenDisconnected) {
                for (LifecycleListener lifecycleListener : lifecycleListeners) {
                    lifecycleListener.onDisconnect(this);
                }
            }
        }
    }
    
    private void listenForEventPackets() throws IOException {
        ByteArrayInputStream inputStream = channel.getInputStream();
        boolean completeShutdown = false;
        try {
            while (inputStream.peek() != -1) {
                int packetLength = inputStream.readInteger(3);
                inputStream.skip(1); // 1 byte for sequence
                int marker = inputStream.read();
                if (marker == 0xFF) {
                    ErrorPacket errorPacket = new ErrorPacket(inputStream.read(packetLength - 1));
                    throw new ServerException(errorPacket.getErrorMessage(), errorPacket.getErrorCode(),
                        errorPacket.getSqlState());
                }
                if (marker == 0xFE && !blocking) {
                    completeShutdown = true;
                    break;
                }
                Event event;
                try {
                    event = eventDeserializer.nextEvent(packetLength == MAX_PACKET_LENGTH ?
                        new ByteArrayInputStream(readPacketSplitInChunks(inputStream, packetLength - 1)) :
                        inputStream);
                    if (event == null) {
                        throw new EOFException();
                    }
                } catch (Exception e) {
                    Throwable cause = e instanceof EventDataDeserializationException ? e.getCause() : e;
                    if (cause instanceof EOFException || cause instanceof SocketException) {
                        throw e;
                    }
                    if (isConnected()) {
                        for (LifecycleListener lifecycleListener : lifecycleListeners) {
                            lifecycleListener.onEventDeserializationFailure(this, e);
                        }
                    }
                    continue;
                }
                if (isConnected()) {
                    eventLastSeen = System.currentTimeMillis();
                    updateGtidSet(event);
                    notifyEventListeners(event);
                    updateClientBinlogFilenameAndPosition(event);
                }
            }
        } catch (Exception e) {
            if (isConnected()) {
                for (LifecycleListener lifecycleListener : lifecycleListeners) {
                    lifecycleListener.onCommunicationFailure(this, e);
                }
            }
        } finally {
            if (isConnected()) {
                if (completeShutdown) {
                    disconnect(); // initiate complete shutdown sequence (which includes keep alive thread)
                } else {
                    disconnectChannel();
                }
            }
        }
    }
    
    private void updateClientBinlogFilenameAndPosition(Event event) {
        EventHeader eventHeader = event.getHeader();
        EventType eventType = eventHeader.getEventType();
        if (eventType == EventType.ROTATE) {
            RotateEventData rotateEventData = (RotateEventData) EventDataWrapper.internal(event.getData());
            binlogFilename = rotateEventData.getBinlogFilename();
            binlogPosition = rotateEventData.getBinlogPosition();
        } else
        // do not update binlogPosition on TABLE_MAP so that in case of reconnect (using a different instance of
        // client) table mapping cache could be reconstructed before hitting row mutation event
        if (eventType != EventType.TABLE_MAP && eventHeader instanceof EventHeaderV4) {
            EventHeaderV4 trackableEventHeader = (EventHeaderV4) eventHeader;
            long nextBinlogPosition = trackableEventHeader.getNextPosition();
            if (nextBinlogPosition > 0) {
                binlogPosition = nextBinlogPosition;
            }
        }
    }
    
    private void notifyEventListeners(Event event) {
        if (event.getData() instanceof EventDataWrapper) {
            event = new Event(event.getHeader(), ((EventDataWrapper) event.getData()).getExternal());
        }
        for (EventListener eventListener : eventListeners) {
            try {
                eventListener.onEvent(event);
            } catch (Exception e) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, eventListener + " choked on " + event, e);
                }
            }
        }
    }

    
    private void updateGtidSet(Event event) {
        synchronized (gtidSetAccessLock) {
            if (gtidSet == null) {
                return;
            }
        }
        EventHeader eventHeader = event.getHeader();
        switch(eventHeader.getEventType()) {
            case GTID:
                GtidEventData gtidEventData = (GtidEventData) EventDataWrapper.internal(event.getData());
                gtid = gtidEventData.getGtid();
                break;
            case XID:
                commitGtid();
                tx = false;
                break;
            case QUERY:
                QueryEventData queryEventData = (QueryEventData) EventDataWrapper.internal(event.getData());
                String sql = queryEventData.getSql();
                if (sql == null) {
                    break;
                }
                if ("BEGIN".equals(sql)) {
                    tx = true;
                } else
                if ("COMMIT".equals(sql) || "ROLLBACK".equals(sql)) {
                    commitGtid();
                    tx = false;
                } else
                if (!tx) {
                    // auto-commit query, likely DDL
                    commitGtid();
                }
            default:
        }
    }
    
    private void commitGtid() {
        if (gtid != null) {
            synchronized (gtidSetAccessLock) {
                gtidSet.add(gtid);
            }
        }
    }
    private void terminateKeepAliveThread() {
        try {
            keepAliveThreadExecutorLock.lock();
            ExecutorService keepAliveThreadExecutor = this.keepAliveThreadExecutor;
            if (keepAliveThreadExecutor == null) {
                return;
            }
            keepAliveThreadExecutor.shutdownNow();
            while (!awaitTerminationInterruptibly(keepAliveThreadExecutor,
                Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                // ignore
            }
        } finally {
            keepAliveThreadExecutorLock.unlock();
        }
    }
    
    private static boolean awaitTerminationInterruptibly(ExecutorService executorService, long timeout, TimeUnit unit) {
        try {
            return executorService.awaitTermination(timeout, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }
    
    /**
     * Disconnect from the replication stream.
     * Note that this does not cause binlogFilename/binlogPosition to be cleared out.
     * As the result following {@link #connect()} resumes client from where it left off.
     */
    public void disconnect() throws IOException {
        terminateKeepAliveThread();
        terminateConnect();
    }

    /**
     * @return true if client is connected, false otherwise
     */
    public boolean isConnected() {
        return connected;
    }


    private PacketChannel openChannel() throws IOException {
        Socket socket = socketFactory != null ? socketFactory.createSocket() : new Socket();
        socket.connect(new InetSocketAddress(hostname, port), (int) connectTimeout);
        return new PacketChannel(socket);
    }

    private Callable scheduleDisconnectIn(final long timeout) {
        final BinaryLogClient self = this;
        final CountDownLatch connectLatch = new CountDownLatch(1);
        final Thread thread = newNamedThread(new Runnable() {
            @Override
            public void run() {
                try {
                    connectLatch.await(timeout, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.log(Level.WARNING, e.getMessage());
                    }
                }
                if (connectLatch.getCount() != 0) {
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.warning("Failed to establish connection in " + timeout + "ms. " +
                            "Forcing disconnect.");
                    }
                    try {
                        self.disconnectChannel();
                    } catch (IOException e) {
                        if (logger.isLoggable(Level.WARNING)) {
                            logger.log(Level.WARNING, e.getMessage());
                        }
                    }
                }
            }
        }, "blc-disconnect-" + hostname + ":" + port);
        thread.start();
        return new Callable() {

            public Object call() throws Exception {
                connectLatch.countDown();
                thread.join();
                return null;
            }
        };
    }

    private GreetingPacket receiveGreeting() throws IOException {
        byte[] initialHandshakePacket = channel.read();
        if (initialHandshakePacket[0] == (byte) 0xFF /* error */) {
            byte[] bytes = Arrays.copyOfRange(initialHandshakePacket, 1, initialHandshakePacket.length);
            ErrorPacket errorPacket = new ErrorPacket(bytes);
            throw new ServerException(errorPacket.getErrorMessage(), errorPacket.getErrorCode(),
                    errorPacket.getSqlState());
        }
        return new GreetingPacket(initialHandshakePacket);
    }
    
    private void ensureEventDataDeserializer(EventType eventType,
            Class<? extends EventDataDeserializer> eventDataDeserializerClass) {
       EventDataDeserializer eventDataDeserializer = eventDeserializer.getEventDataDeserializer(eventType);
       if (eventDataDeserializer.getClass() != eventDataDeserializerClass &&
           eventDataDeserializer.getClass() != EventDataWrapper.Deserializer.class) {
           EventDataDeserializer internalEventDataDeserializer;
           try {
               internalEventDataDeserializer = eventDataDeserializerClass.newInstance();
           } catch (Exception e) {
               throw new RuntimeException(e);
           }
           eventDeserializer.setEventDataDeserializer(eventType,
               new EventDataWrapper.Deserializer(internalEventDataDeserializer,
                   eventDataDeserializer));
       }
   }
    
    private void authenticate(GreetingPacket greetingPacket) throws IOException {
        int collation = greetingPacket.getServerCollation();
        int packetNumber = 1;

        boolean usingSSLSocket = false;
        if (sslMode != SSLMode.DISABLED) {
            boolean serverSupportsSSL = (greetingPacket.getServerCapabilities() & ClientCapabilities.SSL) != 0;
            if (!serverSupportsSSL && (sslMode == SSLMode.REQUIRED || sslMode == SSLMode.VERIFY_CA ||
                sslMode == SSLMode.VERIFY_IDENTITY)) {
                throw new IOException("MySQL server does not support SSL");
            }
            if (serverSupportsSSL) {
                SSLRequestCommand sslRequestCommand = new SSLRequestCommand();
                sslRequestCommand.setCollation(collation);
                channel.write(sslRequestCommand, packetNumber++);
                SSLSocketFactory sslSocketFactory =
                    this.sslSocketFactory != null ?
                        this.sslSocketFactory :
                        sslMode == SSLMode.REQUIRED || sslMode == SSLMode.PREFERRED ?
                            DEFAULT_REQUIRED_SSL_MODE_SOCKET_FACTORY :
                            DEFAULT_VERIFY_CA_SSL_MODE_SOCKET_FACTORY;
                channel.upgradeToSSL(sslSocketFactory,
                    sslMode == SSLMode.VERIFY_IDENTITY ? new TLSHostnameVerifier() : null);
                usingSSLSocket = true;
            }
        }
        AuthenticateCommand authenticateCommand = new AuthenticateCommand(schema, username, password,
            greetingPacket.getScramble());
        authenticateCommand.setCollation(collation);
        channel.write(authenticateCommand, packetNumber);
        byte[] authenticationResult = channel.read();
        if (authenticationResult[0] != (byte) 0x00 /* ok */) {
            if (authenticationResult[0] == (byte) 0xFF /* error */) {
                byte[] bytes = Arrays.copyOfRange(authenticationResult, 1, authenticationResult.length);
                ErrorPacket errorPacket = new ErrorPacket(bytes);
                throw new AuthenticationException(errorPacket.getErrorMessage(), errorPacket.getErrorCode(),
                    errorPacket.getSqlState());
            } else if (authenticationResult[0] == (byte) 0xFE) {
                switchAuthentication(authenticationResult, usingSSLSocket);
            } else {
                throw new AuthenticationException("Unexpected authentication result (" + authenticationResult[0] + ")");
            }
        }
    }
    
    private void switchAuthentication(byte[] authenticationResult, boolean usingSSLSocket) throws IOException {
        /*
            Azure-MySQL likes to tell us to switch authentication methods, even though
            we haven't advertised that we support any.  It uses this for some-odd
            reason to send the real password scramble.
        */
        ByteArrayInputStream buffer = new ByteArrayInputStream(authenticationResult);
        buffer.read(1);

        String authName = buffer.readZeroTerminatedString();
        if ("mysql_native_password".equals(authName)) {
            String scramble = buffer.readZeroTerminatedString();

            Command switchCommand = new AuthenticateNativePasswordCommand(scramble, password);
            channel.write(switchCommand, (usingSSLSocket ? 4 : 3));
            byte[] authResult = channel.read();

            if (authResult[0] != (byte) 0x00) {
                byte[] bytes = Arrays.copyOfRange(authResult, 1, authResult.length);
                ErrorPacket errorPacket = new ErrorPacket(bytes);
                throw new AuthenticationException(errorPacket.getErrorMessage(), errorPacket.getErrorCode(),
                    errorPacket.getSqlState());
            }
        } else {
            throw new AuthenticationException("Unsupported authentication type: " + authName);
        }
    }

    
    private String fetchGtidPurged() throws IOException {
        channel.write(new QueryCommand("show global variables like 'gtid_purged'"));
        ResultSetRowPacket[] resultSet = readResultSet();
        if (resultSet.length != 0) {
            return resultSet[0].getValue(1).toUpperCase();
        }
        return "";
    }
    
    private void fetchBinlogFilenameAndPosition() throws IOException {
        ResultSetRowPacket[] resultSet;
        channel.write(new QueryCommand("show master status"));
        resultSet = readResultSet();
        if (resultSet.length == 0) {
            throw new IOException("Failed to determine binlog filename/position");
        }
        ResultSetRowPacket resultSetRow = resultSet[0];
        binlogFilename = resultSetRow.getValue(0);
        binlogPosition = Long.parseLong(resultSetRow.getValue(1));
    }
    
    private ChecksumType fetchBinlogChecksum() throws IOException {
        channel.write(new QueryCommand("show global variables like 'binlog_checksum'"));
        ResultSetRowPacket[] resultSet = readResultSet();
        if (resultSet.length == 0) {
            return ChecksumType.NONE;
        }
        return ChecksumType.valueOf(resultSet[0].getValue(1).toUpperCase());
    }
    
    private ResultSetRowPacket[] readResultSet() throws IOException {
        List<ResultSetRowPacket> resultSet = new LinkedList<ResultSetRowPacket>();
        byte[] statementResult = channel.read();
        if (statementResult[0] == (byte) 0xFF /* error */) {
            byte[] bytes = Arrays.copyOfRange(statementResult, 1, statementResult.length);
            ErrorPacket errorPacket = new ErrorPacket(bytes);
            throw new ServerException(errorPacket.getErrorMessage(), errorPacket.getErrorCode(),
                    errorPacket.getSqlState());
        }
        while ((channel.read())[0] != (byte) 0xFE /* eof */) { /* skip */ }
        for (byte[] bytes; (bytes = channel.read())[0] != (byte) 0xFE /* eof */; ) {
            resultSet.add(new ResultSetRowPacket(bytes));
        }
        return resultSet.toArray(new ResultSetRowPacket[resultSet.size()]);
    }
    
    private void confirmSupportOfChecksum(ChecksumType checksumType) throws IOException {
        channel.write(new QueryCommand("set @master_binlog_checksum= @@global.binlog_checksum"));
        byte[] statementResult = channel.read();
        if (statementResult[0] == (byte) 0xFF /* error */) {
            byte[] bytes = Arrays.copyOfRange(statementResult, 1, statementResult.length);
            ErrorPacket errorPacket = new ErrorPacket(bytes);
            throw new ServerException(errorPacket.getErrorMessage(), errorPacket.getErrorCode(),
                errorPacket.getSqlState());
        }
        eventDeserializer.setChecksumType(checksumType);
    }
    
    private void enableHeartbeat() throws IOException {
        channel.write(new QueryCommand("set @master_heartbeat_period=" + heartbeatInterval * 1000000));
        byte[] statementResult = channel.read();
        if (statementResult[0] == (byte) 0xFF /* error */) {
            byte[] bytes = Arrays.copyOfRange(statementResult, 1, statementResult.length);
            ErrorPacket errorPacket = new ErrorPacket(bytes);
            throw new ServerException(errorPacket.getErrorMessage(), errorPacket.getErrorCode(),
                errorPacket.getSqlState());
        }
    }
    
    private void requestBinaryLogStream() throws IOException {
        long serverId = blocking ? this.serverId : 0; // http://bugs.mysql.com/bug.php?id=71178
        Command dumpBinaryLogCommand;
        synchronized (gtidSetAccessLock) {
            if (gtidSet != null) {
                dumpBinaryLogCommand = new DumpBinaryLogGtidCommand(serverId,
                    useBinlogFilenamePositionInGtidMode ? binlogFilename : "",
                    useBinlogFilenamePositionInGtidMode ? binlogPosition : 4,
                    gtidSet);
            } else {
                dumpBinaryLogCommand = new DumpBinaryLogCommand(serverId, binlogFilename, binlogPosition);
            }
        }
        channel.write(dumpBinaryLogCommand);
    }
    
    private void disconnectChannel() throws IOException {
        connected = false;
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
    }
    
    private Thread newNamedThread(Runnable runnable, String threadName) {
        Thread thread = threadFactory == null ? new Thread(runnable) : threadFactory.newThread(runnable);
        thread.setName(threadName);
        return thread;
    }

    private void spawnKeepAliveThread() {
        final ExecutorService threadExecutor =
            Executors.newSingleThreadExecutor(new ThreadFactory() {

                @Override
                public Thread newThread(Runnable runnable) {
                    return newNamedThread(runnable, "blc-keepalive-" + hostname + ":" + port);
                }
            });
        try {
            keepAliveThreadExecutorLock.lock();
            threadExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    while (!threadExecutor.isShutdown()) {
                        try {
                            Thread.sleep(keepAliveInterval);
                        } catch (InterruptedException e) {
                            // expected in case of disconnect
                        }
                        if (threadExecutor.isShutdown()) {
                            return;
                        }
                        boolean connectionLost = false;
                        if (heartbeatInterval > 0) {
                            connectionLost = System.currentTimeMillis() - eventLastSeen > keepAliveInterval;
                        } else {
                            try {
                                channel.write(new PingCommand());
                            } catch (IOException e) {
                                connectionLost = true;
                            }
                        }
                        if (connectionLost) {
                            if (logger.isLoggable(Level.INFO)) {
                                logger.info("Trying to restore lost connection to " + hostname + ":" + port);
                            }
                            try {
                                terminateConnect();
                                connect(connectTimeout);
                            } catch (Exception ce) {
                                if (logger.isLoggable(Level.WARNING)) {
                                    logger.warning("Failed to restore connection to " + hostname + ":" + port +
                                        ". Next attempt in " + keepAliveInterval + "ms");
                                }
                            }
                        }
                    }
                }
            });
            keepAliveThreadExecutor = threadExecutor;
        } finally {
            keepAliveThreadExecutorLock.unlock();
        }
    }
    
    private byte[] readPacketSplitInChunks(ByteArrayInputStream inputStream, int packetLength) throws IOException {
        byte[] result = inputStream.read(packetLength);
        int chunkLength;
        do {
            chunkLength = inputStream.readInteger(3);
            inputStream.skip(1); // 1 byte for sequence
            result = Arrays.copyOf(result, result.length + chunkLength);
            inputStream.fill(result, result.length - chunkLength, chunkLength);
        } while (chunkLength == Packet.MAX_LENGTH);
        return result;
    }

    
    /**
     * Connect to the replication stream in a separate thread.
     * @param timeout timeout in milliseconds
     * @throws AuthenticationException if authentication fails
     * @throws ServerException if MySQL server responds with an error
     * @throws IOException if anything goes wrong while trying to connect
     * @throws TimeoutException if client was unable to connect within given time limit
     */
    public void connect(final long timeout) throws IOException, TimeoutException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        AbstractLifecycleListener connectListener = new AbstractLifecycleListener() {
            @Override
            public void onConnect(BinaryLogClient client) {
                countDownLatch.countDown();
            }
        };
        registerLifecycleListener(connectListener);
        final AtomicReference<IOException> exceptionReference = new AtomicReference<IOException>();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    setConnectTimeout(timeout);
                    connect();
                } catch (IOException e) {
                    exceptionReference.set(e);
                    countDownLatch.countDown(); // making sure we don't end up waiting whole "timeout"
                }
            }
        };
        newNamedThread(runnable, "blc-" + hostname + ":" + port).start();
        boolean started = false;
        try {
            started = countDownLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage());
            }
        }
        unregisterLifecycleListener(connectListener);
        if (exceptionReference.get() != null) {
            throw exceptionReference.get();
        }
        if (!started) {
            try {
                terminateConnect();
            } finally {
                throw new TimeoutException("BinaryLogClient was unable to connect in " + timeout + "ms");
            }
        }
    }
    
    /**
     * Unregister single lifecycle listener.
     */
    public void unregisterLifecycleListener(LifecycleListener eventListener) {
        lifecycleListeners.remove(eventListener);
    }

    
    private void terminateConnect() throws IOException {
        do {
            disconnectChannel();
        } while (!tryLockInterruptibly(connectLock, 1000, TimeUnit.MILLISECONDS));
        connectLock.unlock();
    }
    
    /**
     * Unregister single event listener.
     */
    public void unregisterEventListener(EventListener eventListener) {
        eventListeners.remove(eventListener);
    }
    
    /**
     * @param connectTimeout connect timeout in milliseconds.
     * @see #getConnectTimeout()
     */
    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
    private static boolean tryLockInterruptibly(Lock lock, long time, TimeUnit unit) {
        try {
            return lock.tryLock(time, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }
    
    /**
     * Register lifecycle listener. Note that multiple lifecycle listeners will be called in order they
     * where registered.
     */
    public void registerLifecycleListener(LifecycleListener lifecycleListener) {
        lifecycleListeners.add(lifecycleListener);
    }
    
    /**
     * {@link BinaryLogClient}'s event listener.
     */
    public interface EventListener {

        void onEvent(Event event);
    }

    /**
     * {@link BinaryLogClient}'s lifecycle listener.
     */
    public interface LifecycleListener {

        /**
         * Called once client has successfully logged in but before started to receive binlog events.
         */
        void onConnect(BinaryLogClient client);

        /**
         * It's guarantied to be called before {@link #onDisconnect(BinaryLogClient)}) in case of
         * communication failure.
         */
        void onCommunicationFailure(BinaryLogClient client, Exception ex);

        /**
         * Called in case of failed event deserialization. Note this type of error does NOT cause client to
         * disconnect. If you wish to stop receiving events you'll need to fire client.disconnect() manually.
         */
        void onEventDeserializationFailure(BinaryLogClient client, Exception ex);

        /**
         * Called upon disconnect (regardless of the reason).
         */
        void onDisconnect(BinaryLogClient client);
    }
    
    public List<EventListener> getEventListeners() {
        return Collections.unmodifiableList(eventListeners);
    }

    /**
     * Register event listener. Note that multiple event listeners will be called in order they
     * where registered.
     */
    public void registerEventListener(EventListener eventListener) {
        eventListeners.add(eventListener);
    }


    public static abstract class AbstractLifecycleListener implements LifecycleListener {

        public void onConnect(BinaryLogClient client) { }

        public void onCommunicationFailure(BinaryLogClient client, Exception ex) { }

        public void onEventDeserializationFailure(BinaryLogClient client, Exception ex) { }

        public void onDisconnect(BinaryLogClient client) { }

    }
}
