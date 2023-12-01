#include <omnetpp.h>
#include <string>

#include "assignment2_m.h"

class Node: public omnetpp::cSimpleModule {
private:
    omnetpp::cMessage* event = nullptr;
    Assignment2Msg* generateMsg();
    int msgCounter = 0;
protected:
    virtual void initialize() override;
    virtual void handleMessage(omnetpp::cMessage*) override;
public:
    virtual ~Node();
};

Define_Module(Node);

Node::~Node() {
    cancelAndDelete(event);
}

void Node::initialize() {
    if (par("isSenderNode").boolValue()) {
        event = new omnetpp::cMessage("Self message for sender node");
        omnetpp::simtime_t delay {par("sendDelay")};
        scheduleAt(omnetpp::simTime() + delay, event);
    }
}

void Node::handleMessage(omnetpp::cMessage* msg) {
    if (msg->isSelfMessage()) {
        auto new_msg {generateMsg()};
        EV << "Message: " << new_msg->getData() << "Checksum: " << new_msg->getChecksum() << '\n';
        send(new_msg, "out", 0);
        omnetpp::simtime_t delay {par("sendDelay")};
        scheduleAt(omnetpp::simTime() + delay, event);
    } else if (par("isForwarderNode").boolValue()) {
        auto received_msg {omnetpp::check_and_cast<Assignment2Msg*>(msg)};
        auto n {gateSize("out")};
        auto k {intuniform(2, n - 1)};
        send(received_msg, "out", k);
    } else {
        auto received_msg {omnetpp::check_and_cast<Assignment2Msg*>(msg)};
        EV << "Received message from Node#" << received_msg->getNodeId() << " with checksum " << received_msg->getChecksum() << '\n'
                << "Message: " << received_msg->getData() << '\n';
        delete received_msg;
    }
}

Assignment2Msg* Node::generateMsg() {
    auto new_msg {new Assignment2Msg()};
    new_msg->setNodeId(getIndex());
    new_msg->setNodeLocation(getIndex());

    constexpr auto MAX_DATA_SIZE = 256;
    auto raw_data {new char[MAX_DATA_SIZE]};
    snprintf(raw_data, MAX_DATA_SIZE, "Message#%d from Node#%d\n", msgCounter++, getIndex());
    auto checksum = std::hash<std::string> {}(std::string {raw_data});

    new_msg->setData(raw_data);
    delete[] raw_data;

    new_msg->setChecksum(checksum);

    return new_msg;
}
