simple Node {
    parameters:
        @display("i=block/routing");
        bool isSenderNode = default(false);
        bool isForwarderNode = default(false);
        volatile double sendDelay @unit(s) = default(0s);
	gates:
	    input in[];
	    output out[];
}

network Assignment2 {
    
    types:
        channel Channel extends ned.DelayChannel {
        	delay = 100ms;
        }
        
    submodules:
		nodes[5]: Node;
		
	connections:
		nodes[0].out++ --> Channel --> nodes[2].in++;
		nodes[0].in++ <-- Channel <-- nodes[2].out++;
		
		nodes[1].out++ --> Channel --> nodes[2].in++;
		nodes[1].in++ <-- Channel <-- nodes[2].out++;
		
		nodes[2].out++ --> Channel --> nodes[3].in++;
		nodes[2].in++ <-- Channel <-- nodes[3].out++;
		
		nodes[2].out++ --> Channel --> nodes[4].in++;
		nodes[2].in++ <-- Channel <-- nodes[4].out++;
}