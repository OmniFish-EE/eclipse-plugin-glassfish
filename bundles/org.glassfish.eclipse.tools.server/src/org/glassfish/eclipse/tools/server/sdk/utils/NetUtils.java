/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2022 XXXXX Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.glassfish.eclipse.tools.server.sdk.utils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import org.glassfish.eclipse.tools.server.sdk.GlassFishIdeException;
import org.glassfish.eclipse.tools.server.sdk.logging.Logger;

/**
 * Networking utilities
 * <p/>
 *
 * @author Tomas Kraus
 */
public class NetUtils {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Comparator for {@link InetAddress} instances to be sorted.
     */
    public static class InetAddressComparator
            implements Comparator<InetAddress> {

        /**
         * Compares values of <code>InetAddr</code> instances.
         * <p/>
         *
         * @param ip1 First <code>InetAddr</code> instance to be compared.
         * @param ip2 Second <code>InetAddr</code> instance to be compared.
         * @return A negative integer, zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.
         */
        @Override
        public int compare(final InetAddress ip1, final InetAddress ip2) {
            byte[] addr1 = ip1.getAddress();
            byte[] addr2 = ip2.getAddress();
            int result = addr2.length - addr1.length;
            if (result == 0) {
                for (int i = 0; result == 0 && i < addr1.length; i++) {
                    result = addr1[i] - addr2[i];
                }
            }
            return result;
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(ServerUtils.class);

    /** Port check timeout [ms]. */
    public static final int PORT_CHECK_TIMEOUT = 2000;

    /**
     * This is the test query used to ping the server in an attempt to determine if it is secure or not.
     */
    private static byte[] TEST_QUERY = new byte[] {
            // The following SSL query is from nmap (http://www.insecure.org)
            // This HTTPS request should work for most (all?) HTTPS servers
            (byte) 0x16, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 'S', (byte) 0x01,
            (byte) 0x00, (byte) 0x00, (byte) 'O', (byte) 0x03, (byte) 0x00, (byte) '?',
            (byte) 'G', (byte) 0xd7, (byte) 0xf7, (byte) 0xba, (byte) ',', (byte) 0xee,
            (byte) 0xea, (byte) 0xb2, (byte) '`', (byte) '~', (byte) 0xf3, (byte) 0x00,
            (byte) 0xfd, (byte) 0x82, (byte) '{', (byte) 0xb9, (byte) 0xd5, (byte) 0x96,
            (byte) 0xc8, (byte) 'w', (byte) 0x9b, (byte) 0xe6, (byte) 0xc4, (byte) 0xdb,
            (byte) '<', (byte) '=', (byte) 0xdb, (byte) 'o', (byte) 0xef, (byte) 0x10,
            (byte) 'n', (byte) 0x00, (byte) 0x00, (byte) '(', (byte) 0x00, (byte) 0x16,
            (byte) 0x00, (byte) 0x13, (byte) 0x00, (byte) 0x0a, (byte) 0x00, (byte) 'f',
            (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 'e',
            (byte) 0x00, (byte) 'd', (byte) 0x00, (byte) 'c', (byte) 0x00, (byte) 'b',
            (byte) 0x00, (byte) 'a', (byte) 0x00, (byte) '`', (byte) 0x00, (byte) 0x15,
            (byte) 0x00, (byte) 0x12, (byte) 0x00, (byte) 0x09, (byte) 0x00, (byte) 0x14,
            (byte) 0x00, (byte) 0x11, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x06,
            (byte) 0x00, (byte) 0x03, (byte) 0x01, (byte) 0x00,
            // The following is a HTTP request, some HTTP servers won't
            // respond unless the following is also sent
            (byte) 'G', (byte) 'E', (byte) 'T', (byte) ' ', (byte) '/',
            // change the detector to request something that the monitor knows to filter
            // out. This will work-around 109891. Use the longest filtered prefix to
            // avoid false positives....
            (byte) 'c', (byte) 'o', (byte) 'm', (byte) '_', (byte) 's', (byte) 'u',
            (byte) 'n', (byte) '_', (byte) 'w', (byte) 'e', (byte) 'b', (byte) '_',
            (byte) 'u', (byte) 'i',
            (byte) ' ',
            (byte) 'H', (byte) 'T', (byte) 'T', (byte) 'P', (byte) '/', (byte) '1',
            (byte) '.', (byte) '0', (byte) '\n', (byte) '\n'
    };

    /** Comparator for {@link InetAddress} instances to be sorted. */
    private static final InetAddressComparator INET_ADDRESS_COMPARATOR = new InetAddressComparator();

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Finds out if server is running on remote host by connecting to remote host and port.
     * <p/>
     *
     * @param host Server host.
     * @param port Server port.
     * @param timeout Network connection timeout [ms].
     * @return Returns <code>true</code> when server port is accepting connections or <code>false</code>
     * otherwise.
     */
    public static boolean isPortListeningRemote(final String host,
            final int port, final int timeout) {
        final String METHOD = "isPortListeningRemote";
        if (null == host) {
            return false;
        }
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException ex) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioe) {
                    LOGGER.log(Level.INFO, METHOD,
                            "closeError", ioe.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Finds out if server is running on remote host by connecting to remote host and port.
     * <p/>
     *
     * @param host Server host.
     * @param port Server port.
     * @return Returns <code>true</code> when server port is accepting connections or <code>false</code>
     * otherwise.
     */
    public static boolean isPortListeningRemote(final String host,
            final int port) {
        return isPortListeningRemote(host, port, 0);
    }

    /**
     * Finds out if server is running on local host by binding to local port.
     * <p/>
     *
     * @param host Server host or <code>null</code> value for address of the loopback interface.
     * @param port Server port.
     * @return Returns <code>true</code> when server port is accepting connections or <code>false</code>
     * otherwise.
     */
    public static boolean isPortListeningLocal(final String host,
            final int port) {
        final String METHOD = "isPortListeningLocal";
        ServerSocket socket = null;
        try {
            InetAddress ia = InetAddress.getByName(host);
            socket = new ServerSocket(port, 1, ia);
            return false;
        } catch (IOException ioe) {
            return true;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioe) {
                    LOGGER.log(Level.INFO, METHOD,
                            "closeError", ioe.getLocalizedMessage());
                }
            }
        }
    }

    public static boolean isHttpPortListeningLocal(final String host,
            final int port) {
        try {
            URLConnection conn = new URL("http://" + host + ":" + port).openConnection();
            conn.connect();
            return true;
        } catch (IOException ioe) {
            // ioe.printStackTrace();
            return false;
        }
    }

    /**
     * Checks whether the host points to local machine.
     * <p/>
     * Dealing only with simple cases.
     * <p/>
     *
     * @param host Host name to be checked.
     */
    public static boolean isLocahost(String host) {
        if (host == null || host.equals("")) {
            return false;
        }

        host = host.toLowerCase();
        if ("localhost".equals(host) || "127.0.0.1".equals(host) || "::1".equals(host)) {
            return true;
        }

        InetAddress localHostaddr;
        try {
            // Check simple cases.
            localHostaddr = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            java.util.logging.Logger.getLogger(ServerUtils.class.getName()).log(Level.SEVERE, null, ex);
            // TODO add proper exception handling -
            // propagate checked exception to client code.
            return true;
        }
        if (host.equals(localHostaddr.getHostName().toLowerCase())
                || host.equals(localHostaddr.getHostAddress().toLowerCase())) {
            return true;
        }
        return false;
    }

    /**
     * Determine whether an HTTP listener is secure or not..
     * <p/>
     * This method accepts a host name and port #. It uses this information to attempt to connect to the
     * port, send a test query, analyze the result to determine if the port is secure or is not secure
     * (currently only HTTP / HTTPS is supported). it might emit a warning in the server log for
     * GlassFish cases. No Harm, just an annoying warning, so we need to use this call only when really
     * needed.
     * <p/>
     *
     * @param hostname The host for the HTTP listener.
     * @param port The port for the HTTP listener.
     * @throws IOException
     * @throws SocketTimeoutException
     * @throws ConnectException
     */
    public static boolean isSecurePort(String hostname, int port)
            throws IOException, ConnectException, SocketTimeoutException {
        return isSecurePort(hostname, port, 0);
    }

    /**
     * Determine whether an HTTP listener is secure or not..
     * <p/>
     * This method accepts a host name and port #. It uses this information to attempt to connect to the
     * port, send a test query, analyze the result to determine if the port is secure or is not secure
     * (currently only HTTP / HTTPS is supported). it might emit a warning in the server log for
     * GlassFish cases. No Harm, just an annoying warning, so we need to use this call only when really
     * needed.
     * <p/>
     *
     * @param hostname The host for the HTTP listener.
     * @param port The port for the HTTP listener.
     * @param depth Method calling depth.
     * @throws IOException
     * @throws SocketTimeoutException
     * @throws ConnectException
     */
    private static boolean isSecurePort(String hostname, int port, int depth)
            throws IOException, ConnectException, SocketTimeoutException {
        final String METHOD = "isSecurePort";
        boolean isSecure;
        try (Socket socket = new Socket()) {
            try {
                LOGGER.log(Level.FINE, METHOD, "socket");
                socket.connect(new InetSocketAddress(hostname, port), PORT_CHECK_TIMEOUT);
                socket.setSoTimeout(PORT_CHECK_TIMEOUT);
                // This could be bug 70020 due to SOCKs proxy not having localhost
            } catch (SocketException ex) {
                String socksNonProxyHosts = System.getProperty("socksNonProxyHosts");
                if (socksNonProxyHosts != null && socksNonProxyHosts.indexOf("localhost") < 0) {
                    String localhost = socksNonProxyHosts.length() > 0 ? "|localhost" : "localhost";
                    System.setProperty("socksNonProxyHosts", socksNonProxyHosts + localhost);
                    if (depth < 1) {
                        socket.close();
                        return isSecurePort(hostname, port, 1);
                    } else {
                        socket.close();
                        ConnectException ce = new ConnectException();
                        ce.initCause(ex);
                        throw ce; // status unknow at this point
                        // next call, we'll be ok and it will really detect if we are secure or not
                    }
                }
            }
            java.io.OutputStream ostream = socket.getOutputStream();
            ostream.write(TEST_QUERY);
            java.io.InputStream istream = socket.getInputStream();
            byte[] input = new byte[8192];
            istream.read(input);
            String response = new String(input).toLowerCase(Locale.ENGLISH);
            isSecure = true;
            if (response.length() == 0) {
                // isSecure = false;
                // Close the socket
                socket.close();
                throw new ConnectException();
            } else if (response.startsWith("http/1.1 302 moved temporarily")) {
                // 3.1 has started to use redirects... but 3.0 is still using the older strategies...
                isSecure = true;
            } else if (response.startsWith("http/1.")) {
                isSecure = false;
            } else if (response.indexOf("<html") != -1) {
                isSecure = false;
            } else if (response.indexOf("</html") != -1) {
                // New test added to resolve 106245
                // when the user has the IDE use a proxy (like webcache.foo.bar.com),
                // the response comes back as "d><title>....</html>". It looks like
                // something eats the "<html><hea" off the front of the data that
                // gets returned.
                //
                // This test makes an allowance for that behavior. I figure testing
                // the likely "last bit" is better than testing a bit that is close
                // to the data that seems to get eaten.
                //
                isSecure = false;
            } else if (response.indexOf("connection: ") != -1) {
                isSecure = false;
            }
        }
        return isSecure;
    }

    /**
     * Retrieve {@link Set} of IP addresses of this host.
     * <p/>
     *
     * @return {@link Set} of IP addresses of this host.
     * @throws GlassFishIdeException if addresses of this host could not be retrieved.
     */
    public static Set<InetAddress> getHostIPs() {
        final String METHOD = "getHostIPs";
        Set<InetAddress> addrs = new TreeSet<>(INET_ADDRESS_COMPARATOR);
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                List<InterfaceAddress> iAddrs = iface.getInterfaceAddresses();
                for (InterfaceAddress iAddr : iAddrs) {
                    addrs.add(iAddr.getAddress());
                }
            }
        } catch (SocketException se) {
            addrs = null;
            throw new GlassFishIdeException(LOGGER.excMsg(METHOD, "exception"));
        }
        return addrs;
    }

    /**
     * Retrieve {@link Set} of IPv4 addresses of this host.
     * <p/>
     *
     * @return {@link Set} of IPv4 addresses of this host.
     */
    public static Set<Inet4Address> getHostIP4s() {
        final String METHOD = "getHostIP4s";
        Set<Inet4Address> addrs = new TreeSet<>(INET_ADDRESS_COMPARATOR);
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                List<InterfaceAddress> iAddrs = iface.getInterfaceAddresses();
                for (InterfaceAddress iAddr : iAddrs) {
                    InetAddress addr = iAddr.getAddress();
                    if (addr instanceof Inet4Address) {
                        addrs.add((Inet4Address) addr);
                    }
                }
            }
        } catch (SocketException se) {
            addrs = null;
            throw new GlassFishIdeException(LOGGER.excMsg(METHOD, "exception"));
        }
        return addrs;
    }

    /**
     * Retrieve {@link Set} of IPv6 addresses of this host.
     * <p/>
     *
     * @return {@link Set} of IPv6 addresses of this host.
     */
    public static Set<Inet6Address> getHostIP6s() {
        final String METHOD = "getHostIP6s";
        Set<Inet6Address> addrs = new TreeSet<>(INET_ADDRESS_COMPARATOR);
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                List<InterfaceAddress> iAddrs = iface.getInterfaceAddresses();
                for (InterfaceAddress iAddr : iAddrs) {
                    InetAddress addr = iAddr.getAddress();
                    if (addr instanceof Inet6Address) {
                        addrs.add((Inet6Address) addr);
                    }
                }
            }
        } catch (SocketException se) {
            addrs = null;
            throw new GlassFishIdeException(LOGGER.excMsg(METHOD, "exception"));
        }
        return addrs;
    }

}
