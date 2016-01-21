import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.CyclicBarrier;

public class Main {

    public static final int MIN_PORTS_PER_THREAD = 20;
    public static final int MAX_THREAD = 0xFF;

    private static InetAddress inetAddress;
    private static List<Integer> allPorts;

    private static List<Integer> allOpenPorts = new ArrayList<>();
    private static List<PortScanWorker> workers = new ArrayList<>(MAX_THREAD);

    private static Date startTime;
    private static Date endTime;

    private void start() {
        readArgs();

        startTime = new Date();

        if(allPorts.size() / MIN_PORTS_PER_THREAD > MAX_THREAD) {
            final int PORT_PER_THREAD = allPorts.size() / MAX_THREAD;
            addWorkers(PORT_PER_THREAD);
        } else {
            addWorkers(MIN_PORTS_PER_THREAD);
        }

        System.out.println("Ports to scan: " + allPorts.size());
        System.out.println("Threads to work: " + workers.size());

        Runnable summarizer = new Runnable() {
            @Override
            public void run() {
                System.out.println("Scanning stopped.");

                for(PortScanWorker psw : workers) {
                    List<Integer> openPorts = psw.getOpenPorts();
                    allPorts.addAll(openPorts);
                }

                Collections.sort(allOpenPorts);

                if(allOpenPorts.size() == 0) {
                    System.out.println("Scanned ports are closed.");
                } else {
                    System.out.println("List of opened ports: ");
                    for (Integer openedPort : allOpenPorts) {
                        System.out.println(openedPort);
                    }
                }

                endTime = new Date();

                System.out.println("Elapsed time: " + (endTime.getTime() - startTime.getTime()) + " ms");
            }
        };

        CyclicBarrier barrier = new CyclicBarrier(workers.size(), summarizer);

        for(PortScanWorker psw : workers) {
            psw.setBarrier(barrier);
        }

        System.out.println("Start scanning...");

        for(PortScanWorker psw : workers) {
            new Thread(psw).start();
        }
    }

    private void addWorkers(int portPerThread) {
        List<Integer> threadPorts = new ArrayList<>();
        for(int i = 0, counter = 0; i < allPorts.size(); i++, counter++) {
            if(counter < portPerThread) {
                threadPorts.add(allPorts.get(i));
            } else {
                PortScanWorker psw = new PortScanWorker();
                psw.setInetAddress(inetAddress);
                psw.setPorts(new ArrayList<>(threadPorts));
                workers.add(psw);
                threadPorts.clear();
                counter = 0;
            }
        }

        PortScanWorker psw = new PortScanWorker();
        psw.setInetAddress(inetAddress);
        psw.setPorts(new ArrayList<>(threadPorts));
        workers.add(psw);
    }

    private void readArgs() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please, enter IP-address and/or ports to scan:");
        String s = sc.nextLine();
        sc.close();

        processArgs(s);
    }

    private void processArgs(String s) {
        if("".equals(s)) {
            usage();
            System.exit(1);
        }
        String[] args = s.split(" ");

        String host = args[0];
        try {
            inetAddress = InetAddress.getByName(host);
        } catch(UnknownHostException e) {
            System.out.println("Error when resolving host!");
            System.exit(2);
        }

        System.out.println("Scanning host: " + host);

        int minPort = 0;
        int maxPort = 0x10000-1;

        if(args.length == 2) {
            if(args[1].indexOf("-") > -1) {
                String[] ports = args[1].split("-");
                try {
                    minPort = Integer.parseInt(ports[0]);
                    maxPort = Integer.parseInt(ports[1]);
                } catch(NumberFormatException e) {
                    System.out.println("Wrong ports!");
                    System.exit(3);
                }
            } else {
                try {
                    minPort = Integer.parseInt(args[1]);
                    maxPort = minPort;
                } catch(NumberFormatException e) {
                    System.out.println("Wrong ports!");
                    System.exit(3);
                }
            }
        }

        allPorts = new ArrayList<>(maxPort - minPort + 1);

        for(int i = minPort; i <= maxPort; i++) {
            allPorts.add(i);
        }
    }

    private void usage() {
        System.out.println("Java Port Scanner usage");
        System.out.println("Examples: ");
        System.out.println("192.168.1.1 1-2048");
        System.out.println("192.168.1.1 8080");
        System.out.println("192.168.1.1");
    }

    public static void main(String[] args) {
        new Main().start();
    }

}
