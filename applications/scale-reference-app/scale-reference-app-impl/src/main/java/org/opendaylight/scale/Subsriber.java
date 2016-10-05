package org.opendaylight.scale;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;

/**
 * Created by evijayd on 9/26/2016.
 */
public class Subsriber {

    private long id = 0;
    private Ipv4Prefix ipv4Prefix = new Ipv4Prefix("127.0.0.1/32");
    private int vni = 0;
    private int portStart = 0;
    private int portEnd = 0;
    private int priority = 0;
    private int profileId = 0;
    private String displayName = "";

    private Subsriber() {
    }

    public static Subsriber create() {
        return new Subsriber();
    }

    public String getDisplayName() {
        return displayName;
    }

    public Subsriber setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public long getId() {
        return id;
    }

    public Subsriber setId(long id) {
        this.id = id;
        return this;
    }

    public Ipv4Prefix getIpv4Prefix() {
        return ipv4Prefix;
    }

    public Subsriber setIpv4Prefix(Ipv4Prefix ipv4Prefix) {
        this.ipv4Prefix = ipv4Prefix;
        return this;
    }

    public int getPortEnd() {
        return portEnd;
    }

    public Subsriber setPortEnd(int portEnd) {
        this.portEnd = portEnd;
        return this;
    }

    public int getPortStart() {
        return portStart;
    }

    public Subsriber setPortStart(int portStart) {
        this.portStart = portStart;
        return this;
    }

    public int getPriority() {
        return priority;
    }

    public Subsriber setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public int getProfileId() {
        return profileId;
    }

    public Subsriber setProfileId(int profileId) {
        this.profileId = profileId;
        return this;
    }

    public int getVni() {
        return vni;
    }

    public Subsriber setVni(int vni) {
        this.vni = vni;
        return this;
    }

    @Override
    public String toString() {
        return "Subsriber{" +
                "displayName='" + displayName + '\'' +
                ", id=" + id +
                ", ipv4Prefix=" + ipv4Prefix +
                ", portEnd=" + portEnd +
                ", portStart=" + portStart +
                ", priority=" + priority +
                ", profileId=" + profileId +
                ", vni=" + vni +
                '}';
    }
}
