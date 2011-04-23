package edu.washington.cs.knowitall.index;

import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.RemoteSearchable;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * <p>
 * This class is used to host a Lucene index, and make it available to clients
 * via Java's Remote Method Invocation (RMI) protocol. It is built around the 
 * {@link RemoteSearchable} class from Lucene's contrib-remote package.
 * </p>
 * 
 * <p>
 * The behavior of this class is defined by three parameters: a path to a
 * Lucene index directory, the name of the RMI service (defaults to 
 * {@link IndexServer#DEFAULT_NAME}), and the port of the RMI registry 
 * (defaults to {@link IndexServer#DEFAULT_PORT}). After constructing a new
 * instance, the {@link IndexServer#startServer()} method can be called to
 * start the server.
 * </p>
 * 
 * <p>
 * An {@linkplain IndexServer} can be started on the command line by calling
 * this class, passing it a single parameter that is the path to the Lucene
 * index directory.
 * </p>
 * @author afader
 *
 */
public class IndexServer {
    
    /**
     * The default RMI port.
     */
    public static final int DEFAULT_PORT = 1099;
    
    /**
     * The default name for the RMI service.
     */
    public static final String DEFAULT_NAME = "IndexServer";
    
    private int port = DEFAULT_PORT;
    private String name = DEFAULT_NAME;
    private File indexPath;
    private boolean started = false;
    private Registry reg;
    
    /**
     * Constructs a new server.
     * @param indexPath
     * @param name
     * @param port
     */
    public IndexServer(File indexPath, String name, int port) {
        this.indexPath = indexPath;
        this.name = name;
        this.port = port;
    }
    
    /**
     * Constructs a new server using the default port.
     * @param indexPath
     * @param name
     */
    public IndexServer(File indexPath, String name) {
        this(indexPath, name, DEFAULT_PORT);
    }
    
    /**
     * Constructs a new server using the default port and default name.
     * @param indexPath
     */
    public IndexServer(File indexPath) {
        this(indexPath, DEFAULT_NAME, DEFAULT_PORT);
    }
    
    /**
     * Constructs a new server using the default name.
     * @param indexPath
     * @param port
     */
    public IndexServer(File indexPath, int port) {
        this(indexPath, DEFAULT_NAME, port);
    }
    
    /**
     * @return the path to the index
     */
    public File getIndexPath() {
        return indexPath;
    }
    
    /**
     * @return the RMI port
     */
    public int getPort() {
        return port;
    }
    
    /**
     * @return the RMI name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return the full RMI name (e.g. {@code //localhost:1099/IndexServer})
     */
    public String getFullName() {
        return String.format("//localhost:%s/%s", getPort(), getName());
    }
    
    /**
     * @return true if the server has been started
     */
    public boolean isStarted() {
        return started;
    }
    
    /**
     * Starts the server
     * @throws CorruptIndexException if unable to open the index
     * @throws IOException if unable to open the index or start the server
     */
    public void startServer() throws CorruptIndexException, IOException {
        
        // Open up an index searcher that reads from the local index
        Directory dir = FSDirectory.open(getIndexPath());
        Searchable local = new IndexSearcher(dir);
        
        // Wrap it with the RemoteSearchable class, which allows for RMI
        RemoteSearchable remote = new RemoteSearchable(local);

        // Make the index available for RMI with the given port and name
        reg = LocateRegistry.createRegistry(getPort());
        reg.rebind(getName(), remote);
        started = true;
        
    }
    
    /**
     * Stops the server
     * @throws IOException
     * @throws NotBoundException if unable to stop the server, or if it was not
     * started
     */
    public void stopServer() throws IOException {
        if (started) {
            try {
                reg.unbind(getName());
                started = false;
            } catch (NotBoundException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * Starts a {@link IndexServer} using the given index (passed via args[0])
     * on the default port with the default RMI name.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) {
        
        if (args.length != 1) {
            System.err.println("Usage: IndexServer indexPath");
            System.exit(1);
        }
        
        File indexPath = new File(args[0]);
        IndexServer server = new IndexServer(indexPath);
        
       
        try {
            System.err.println("Starting server at " + server.getFullName());
            server.startServer();
            System.err.println("Server started");
        } catch (Exception e) {
            System.err.println("Could not start server!");
            e.printStackTrace(System.err);
            System.exit(1);
        }
        
    }

}
