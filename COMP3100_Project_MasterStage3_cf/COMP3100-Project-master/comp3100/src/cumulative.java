public class cumulative(ArrayList<Server> servers) {

        String msg = readFromStream();

        ServerInfo server = null;
        Job job = null;
       
            while(!getCommand(msg).equals("NONE")){
            if(getCommand(msg).equals("OK"))
                redy();

            if(getCommand(msg).equals("JOBN") || getCommand(msg).equals("JOBP")){
                job = getJob(msg);

                //ServerInfo cumFit = new ServerInfo();
                //ServerInfo newFit = new ServerInfo();

                rescCapable(job.cores, job.memory, job.disk);
                msg = readFromStream();

                if(getCommand(msg).equals("DATA")){
                    ok();
                    msg = readFromStream();
                                        
                    int cumFit = Integer.MAX_VALUE;
                    boolean found= false;

                    while(!getCommand(msg).equals(".")){
                        server = getServerInfo(msg);
                        //ServerInfo temp = getServerInfo(msg);
                        //found = true;

                        if(Integer.parseInt(temp.state) < 4){
                            if(Integer.parseInt(temp.coreCount) > 0 ){
                                server = getServerInfo(msg);
                                    int coreFit = Integer.parseInt(server.cores) -Integer.parseInt(job.coreCount);
                                    int memFit =  (Integer.parseInt(server.memory) -Integer.parseInt(job.memory))/2^10;
                                    int diskFit = (Integer.parseInt(server.disk) -Integer.parseInt(job.disk)) /2^10;

                                if(coreFit >= 0 && memFit >= 0 && diskFit >= 0){
                                    int newFit =coreFit + memFit +diskFit;
                                    if (cumFit > newFit){
                                        schd(job.id, server.type, server.id);
                                        found= true;
                                        cumFit = newFit;
                                    }

                                }
                        
                                //smallest = Integer.parseInt(server.coreCount);
                            }
                            ok();
                            msg = readFromStream();
                        }
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
    
}