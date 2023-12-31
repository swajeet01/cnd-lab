//
// Generated file, do not edit! Created by opp_msgtool 6.0 from assignment3.msg.
//

#ifndef __ASSIGNMENT3_M_H
#define __ASSIGNMENT3_M_H

#if defined(__clang__)
#  pragma clang diagnostic ignored "-Wreserved-id-macro"
#endif
#include <omnetpp.h>

// opp_msgtool version check
#define MSGC_VERSION 0x0600
#if (MSGC_VERSION!=OMNETPP_VERSION)
#    error Version mismatch! Probably this file was generated by an earlier version of opp_msgtool: 'make clean' should help.
#endif

class Assignment3Msg;
/**
 * Class generated from <tt>assignment3.msg:1</tt> by opp_msgtool.
 * <pre>
 * message Assignment3Msg
 * {
 *     int nodeId;
 *     int nodeLocation;
 *     string data;
 *     long checksum;
 * }
 * </pre>
 */
class Assignment3Msg : public ::omnetpp::cMessage
{
  protected:
    int nodeId = 0;
    int nodeLocation = 0;
    omnetpp::opp_string data;
    long checksum = 0;

  private:
    void copy(const Assignment3Msg& other);

  protected:
    bool operator==(const Assignment3Msg&) = delete;

  public:
    Assignment3Msg(const char *name=nullptr, short kind=0);
    Assignment3Msg(const Assignment3Msg& other);
    virtual ~Assignment3Msg();
    Assignment3Msg& operator=(const Assignment3Msg& other);
    virtual Assignment3Msg *dup() const override {return new Assignment3Msg(*this);}
    virtual void parsimPack(omnetpp::cCommBuffer *b) const override;
    virtual void parsimUnpack(omnetpp::cCommBuffer *b) override;

    virtual int getNodeId() const;
    virtual void setNodeId(int nodeId);

    virtual int getNodeLocation() const;
    virtual void setNodeLocation(int nodeLocation);

    virtual const char * getData() const;
    virtual void setData(const char * data);

    virtual long getChecksum() const;
    virtual void setChecksum(long checksum);
};

inline void doParsimPacking(omnetpp::cCommBuffer *b, const Assignment3Msg& obj) {obj.parsimPack(b);}
inline void doParsimUnpacking(omnetpp::cCommBuffer *b, Assignment3Msg& obj) {obj.parsimUnpack(b);}


namespace omnetpp {

template<> inline Assignment3Msg *fromAnyPtr(any_ptr ptr) { return check_and_cast<Assignment3Msg*>(ptr.get<cObject>()); }

}  // namespace omnetpp

#endif // ifndef __ASSIGNMENT3_M_H

