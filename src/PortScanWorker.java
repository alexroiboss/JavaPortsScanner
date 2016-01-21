import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class PortScanWorker implements Runnable {
    private static int globalId = 1;

    private int id;
    private List<Integer> ports;
    private List<Integer> openPorts;
    private InetAddress inetAddress;
    private int timeout = 200;
    private CyclicBarrier barrier;

    public PortScanWorker() {
        id = globalId++;
    }

    public void run() {
        scan(inetAddress);
        try {
            barrier.await();
        } catch(InterruptedException e) {
            e.printStackTrace();
        } catch(BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    private void scan(InetAddress inetAddress) {
        openPorts = new ArrayList<>();
        for(Integer port : ports) {
            try(Socket socket = new Socket()) {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, port);
                socket.connect(inetSocketAddress, timeout);
                System.out.println("Found opened port: " + port);
                openPorts.add(port);
            } catch(IOException e) {}
        }
    }

    public int getId() {
        return id;
    }

    public void setPorts(List<Integer> ports) {
        this.ports = ports;
    }

    public List<Integer> getOpenPorts() {
        return openPorts;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setBarrier(CyclicBarrier barrier) {
        this.barrier = barrier;
    }
}
