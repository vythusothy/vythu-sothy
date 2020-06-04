import java.net.*;
import java.util.ArrayList;
import java.io.*;
import Config.*;

public class cumFitFinal{
    public Socket socket;

    public cum(Socket socket){
        this.socket = socket;
    }

    //First scheduling algorithm, sends all jobs to largest server type
    //Accepts servers parsed from system.xml as a parameter
    public void allToLargest(ArrayList<Server> servers){
        //Gets first message after auth to initialize msg
        String msg = readFromStream();

        //Find the largest server by coreCount and set largest to that server
        Server largest = servers.get(0);
        for(Server item: servers){
            int temp = Integer.parseInt(item.coreCount);
            if(temp > Integer.parseInt(largest.coreCount)){
                largest = item;
            }
        }

        //Loop until no jobs left: NONE recieved
        while(!getCommand(msg).equals("NONE")){
            //Send REDY when server sends OK: is ready for next job
            if(getCommand(msg).equals("OK"))
                redy();

            //Send SCHD when server sends JOBN: Schedule a job when one is recieved
            if(getCommand(msg).equals("JOBN")){
                //Takes JOBN command and splits data into fields of a job object
                Job job = getJob(msg);
           //Schedule job: SCHD jobID serverType serverID
                schd(job.id, largest.type, "0");
                
            }
            //Get next message for loop
            msg = readFromStream();
        }
    }

    public void firstFit(ArrayList<Server> servers){
        String msg = readFromStream();

        ServerInfo server = null;
        Job job = null;
       
                while(!getCommand(msg).equals("NONE")){
            if(getCommand(msg).equals("OK"))
                redy();

            if(getCommand(msg).equals("JOBN") || getCommand(msg).equals("JOBP")){
                job = getJob(msg);

                rescCapable(job.cores, job.memory, job.disk);
                msg = readFromStream();

                if(getCommand(msg).equals("DATA")){
                    ok();
                    msg = readFromStream();
                    boolean found= false;//int smallest = 2000;
                    while(!getCommand(msg).equals(".")){
                
                        ServerInfo temp = getServerInfo(msg);
                        found = true;
                        //Check if valid server
                        //if(Integer.parseInt(temp.state) < 4){
                            if(Integer.parseInt(temp.coreCount) > 0 ){
                                server = getServerInfo(msg);
                                //smallest = Integer.parseInt(server.coreCount);
                            }
                            ok();
                            msg = readFromStream();
                        //}
                    }
                    if(found== true)
                    schd(job.id, server.type, server.id);
                    else{
                    nxtj();
                    //kilj(server.type, server.id, job.id);
                    }
                }
                
            }
            msg = readFromStream();
        }
    }

    public void bestFit(ArrayList<Server> servers){
        //Gets first message after auth to initialize msg
        String msg = readFromStream();

        ServerInfo server = null;
        
        


        //vlaues to track best fit an minavial
        

        
        
        //Loop until no jobs left: NONE recieved
        while(!getCommand(msg).equals("NONE")){
            int fitnessValue = Integer.MAX_VALUE;
            int minAvail = Integer.MAX_VALUE;
            Job job = null;
            //Send REDY when server sends OK: is ready for next job
            if(getCommand(msg).equals("OK"))
                redy();

                msg = readFromStream();
            //Send SCHD when server sends JOBN or JOBP: Schedule a job when one is recieved
            if(getCommand(msg).equals("JOBN") || getCommand(msg).equals("JOBP")){
                job = getJob(msg); // get the job info

                //serverinfo object tot server
                ServerInfo bestFit = new ServerInfo();
                //serverinfo object to track default server
                ServerInfo defaultFit = new ServerInfo();
                
                rescAvail(job.cores, job.memory, job.disk);
                
                msg = readFromStream();

                if(getCommand(msg).equals("DATA")){
                    writeToStream("OK");
                    msg = readFromStream();
                    
                    if(!getCommand(msg).equals("."))
                        defaultFit = getServerInfo(msg);
                
                    msg = readFromStream();

                    while(!msg.startsWith(".")){
                        
                        server = getServerInfo(msg);
                        System.out.println(msg);
                        //Check if valid server
                        if(Integer.parseInt(server.state) < 4 && Integer.parseInt(server.coreCount) > 0){                 
                            int tempFit = Integer.parseInt(job.cores) -Integer.parseInt(server.coreCount);
                            //checks to see if server is in correct state
                            if(tempFit>= 0){   
                                if(tempFit < fitnessValue || (tempFit == fitnessValue && Integer.parseInt(server.availableTime) < minAvail && Integer.parseInt(server.availableTime) > -1)){
                                    bestFit = new ServerInfo(server.type, server.id, server.state, server.availableTime, server.coreCount, server.memory, server.disk);
                                    fitnessValue = tempFit;
                                    minAvail = Integer.parseInt(server.availableTime);
                                }
                            }
                            ok();
                            msg = readFromStream();
                            
                        }  
                    }
                    if(!bestFit.type.equals("empty"))
                        schd(job.id, bestFit.type, bestFit.id);
                    else
                        schd(job.id, defaultFit.type, defaultFit.id);
                    
                    msg = readFromStream();  
                }
            }
        }
    }

    public void worstFit(ArrayList<Server> servers){
        //Gets first message after auth to initialize msg
        String msg = readFromStream();

        ServerInfo server = null;
        Job job = null;

        //Loop until no jobs left: NONE recieved
        while(!getCommand(msg).equals("NONE")){
            int fitnessValue = Integer.MIN_VALUE;

            //Send REDY when server sends OK: is ready for next job
            if(getCommand(msg).equals("OK"))
                redy();

            //Send SCHD when server sends JOBN or JOBP: Schedule a job when one is recieved
            if(getCommand(msg).equals("JOBN") || getCommand(msg).equals("JOBP")){
                //Takes JOBN command and splits data into fields of a job object
                job = getJob(msg);

                ServerInfo worstFit = new ServerInfo();
                ServerInfo altFit = new ServerInfo();

                rescCapable(job.cores, job.memory, job.disk);

                msg = readFromStream();

                //Get responses
                if(getCommand(msg).equals("DATA")){
                    ok();
                    msg = readFromStream();

                    //While still recieveing responses
                    while(!getCommand(msg).equals(".")){
                        server = getServerInfo(msg);
                        //Check if valid server
                        if(Integer.parseInt(server.state) < 4 && Integer.parseInt(server.coreCount) > 0){
                            int tempFit = Integer.parseInt(server.coreCount) - Integer.parseInt(job.cores);
                            if(tempFit > fitnessValue && Integer.parseInt(server.availableTime) == Integer.parseInt(job.submitTime)){
                                worstFit = new ServerInfo(server.type, server.id, server.state, server.availableTime, server.coreCount, server.memory, server.disk);
                                fitnessValue = tempFit;
                            } else if(tempFit > fitnessValue && Integer.parseInt(server.availableTime) > -1){
                                altFit = new ServerInfo(server.type, server.id, server.state, server.availableTime, server.coreCount, server.memory, server.disk);
                                fitnessValue = tempFit;
                            }
                        }
                        ok();
                        msg = readFromStream();
                    }
                }

                
                if(!worstFit.type.equals("empty")){
                    schd(job.id, worstFit.type, worstFit.id);
                    System.out.println("wf");
                }else if(!altFit.type.equals("empty")){
                    schd(job.id, altFit.type, altFit.id);
                    System.out.println("af");
                }else {
                    System.out.println("df");
                    Server largest = servers.get(0);
                    for(Server item: servers){
                        int temp = Integer.parseInt(item.coreCount);
                        if(temp > Integer.parseInt(largest.coreCount)){
                            largest = item;
                        }
                    }
                    schd(job.id, largest.type, "0");
                } 
            }
        // Get next message for loop
        msg=readFromStream();

        }
    }

    //HELPER METHODS
    //ALL COMMANDS CAN BE FOUND HERE
    //use to remove clutter from main algorithms

    public void writeToStream(String msg){
        try {
            OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
            out.write(msg);// string, byte[]
            out.flush();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    //Read socket input from server
    public String readFromStream(){
        byte[] readMsg = new byte[1024];
        
        try{
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            in.read(readMsg);   
        } catch(IOException e){
            System.out.println(e);
        }
        
        String message = new String(readMsg);
        return message;
    }

    //Takes server message from readFromStream() and extracts command part
    public String getCommand(String msg){
        String ret = msg;
        if(msg.subSequence(0, 1).equals("."))
            ret = ".";
        if(msg.subSequence(0, 2).equals("OK"))
            ret = "OK";
        if(msg.subSequence(0, 3).equals("ERR"))
            ret = "ERR";
        if(msg.subSequence(0, 4).equals("DATA"))
            ret = "DATA";
        if(msg.subSequence(0, 4).equals("QUIT"))
            ret = "QUIT";
        if(msg.subSequence(0, 4).equals("JOBN"))
            ret = "JOBN";
        if(msg.subSequence(0, 4).equals("JOBP"))
            ret = "JOBP";
        if(msg.subSequence(0, 4).equals("RESF"))
            ret = "RESF";
        if(msg.subSequence(0, 4).equals("RESR"))
            ret = "RESR";
        if(msg.subSequence(0, 4).equals("NONE"))
            ret = "NONE";
        return ret;
    }

    public void helo(){
        writeToStream("HELO");
    }

    //auth should be comp335
    public void auth(String auth){
        writeToStream("AUTH" + auth);
    }

    public void ok(){
        writeToStream("OK");
    }

    //Sends HELO and AUTH commands to the server
    public void wakeUp(){
        helo();
        readFromStream();
        //comp335 makes the test file work
        auth("comp335");
    }

    public void redy(){
        writeToStream("REDY");
    }

    public void rescAll(){
        writeToStream("RESC All");
    }

    public void rescType(String type){
        writeToStream("RESC Type " + type);
    }

    public void rescAvail(String cores, String disk, String memory){
        writeToStream("RESC Avail " + cores + " " + disk + " " + memory);
    }

    public void rescCapable(String cores, String disk, String memory){
        writeToStream("RESC Capable " + cores + " " + disk + " " + memory);
    }

    public void lstj(String type, String id){
        writeToStream("LSTJ " + type + " " + id);
    }

    public void nxtj(){
        writeToStream("NXTJ");
    }

    public void kilj(String type, String sid, String jid){
        writeToStream("KILJ " + type + " " + sid + " " + jid);
    }

    public void term(String type, String sid){
        writeToStream("TERM " + type + " " + sid);
    }

    public void schd(String jid, String type, String sid){
        writeToStream("SCHD " + jid + " " + type + " " + sid);
    }

    public void quit(){
        writeToStream("QUIT");
    }

    public Job getJob(String msg){
        String[] splitter = msg.split(" ");
        Job job = new Job(splitter[1], splitter[2], splitter[3], splitter[4], splitter[5], splitter[6]);
        return job;
    }

    public ServerInfo getServerInfo(String msg){
        String[] splitter = msg.split(" ");
        ServerInfo server = new ServerInfo(splitter[0], splitter[1], splitter[2], splitter[3], splitter[4], splitter[5], splitter[6]);
        return server;
    }
}

