#include <omnetpp.h>
#include <string>
#include <cmath>

#include "assignment3_m.h"

long simpleHash(std::string key) {
    constexpr long PRIME_CONST = 31;
    long hash = 0;
    for (int i = 0; i < key.length(); i++)
        hash += key[i] * std::pow(PRIME_CONST, i);
    return hash;
}

class Source: public omnetpp::cSimpleModule {
    omnetpp::cMessage* event;
public:
    Assignment3Msg* generateMessage();
protected:
    virtual void initialize() override;
    virtual void handleMessage(omnetpp::cMessage*) override;
};

Define_Module(Source);

void Source::initialize() {
    event = new omnetpp::cMessage("Self-message!");
    omnetpp::simtime_t delayTime = par("delayTime");
    scheduleAt(omnetpp::simTime() + delayTime, event);
}

void Source::handleMessage(omnetpp::cMessage* msg) {
    auto send_msg = generateMessage();
    send(send_msg, "out");
    omnetpp::simtime_t delayTime = par("delayTime");
    scheduleAt(omnetpp::simTime() + delayTime, event);
}

Assignment3Msg* Source::generateMessage() {
    auto msg = new Assignment3Msg();
    auto dest = intuniform(1, 2);
    msg->setNodeId(dest);
    msg->setNodeLocation(0);
    std::string data = "Hello Mom!";
    msg->setData(data.c_str());
    auto checksum = simpleHash(data);
    msg->setChecksum(checksum);
    return msg;

}


class Router: public omnetpp::cSimpleModule {
protected:
    virtual void handleMessage(omnetpp::cMessage*) override;
};

Define_Module(Router);

void Router::handleMessage(omnetpp::cMessage* msg) {
    Assignment3Msg* c_msg = omnetpp::check_and_cast<Assignment3Msg*>(msg);
    auto nodeId = c_msg->getNodeId();
    if (nodeId % 2 == 0) { // Even
        bubble("Even");
        send(c_msg, "to_6");
    } else { // Odd
        bubble("Odd");
        send(c_msg, "to_3");
    }
}

class Forwarder: public omnetpp::cSimpleModule {
protected:
    virtual void handleMessage(omnetpp::cMessage*) override;
};

Define_Module(Forwarder);

void Forwarder::handleMessage(omnetpp::cMessage* msg) {
    send(msg, "to_end");
}

class EndNode: public omnetpp::cSimpleModule {
protected:
    virtual void handleMessage(omnetpp::cMessage*) override;
};

Define_Module(EndNode);

void EndNode::handleMessage(omnetpp::cMessage* msg) {
    Assignment3Msg* c_msg = omnetpp::check_and_cast<Assignment3Msg*>(msg);
    auto rec_checksum = c_msg->getChecksum();
    auto cal_checksum = simpleHash(c_msg->getData());
    if (rec_checksum == cal_checksum) {
        EV << "Message was not altered.\n";
    }

    delete msg;
}
