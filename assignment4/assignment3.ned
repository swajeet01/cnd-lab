simple Source {
    parameters:
        volatile double delayTime = 0.2s @unit(s);
    gates:
		output out;
}

simple Router {
    gates:
		input in;
		output to_3;
		output to_6;
}

simple Forwarder {
	gates:
	    input from_router;
	    output to_end;
}

simple EndNode {
	gates:
	    input in;
}

network Assignment3 {
	submodules:
	    source: Source;
	    router: Router;
	    forwarders[2]: Forwarder;
	    endNodes[2]: EndNode;
	connections:
	    source.out --> { delay = 100ms; } --> router.in;
	    router.to_3 --> { delay = 100ms; } --> forwarders[0].from_router;
	    router.to_6 --> { delay = 100ms; } --> forwarders[1].from_router;
	    forwarders[0].to_end --> { delay = 100ms; } --> endNodes[0].in;
	    forwarders[1].to_end --> { delay = 100ms; } --> endNodes[1].in;
	    
}